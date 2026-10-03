package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.modules.pacientes.parentesco.parentescoEntity;
import com.example.demo.modules.pacientes.parentesco.parentescoRepository;
import com.example.demo.modules.pacientes.institucion.institucionEntity;
import com.example.demo.modules.pacientes.institucion.institucionRepository;
import com.example.demo.modules.pacientes.camas.camasEntity;
import com.example.demo.modules.pacientes.camas.camasRepository;
import com.example.demo.modules.pacientes.camas.EstadoCama;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PersonaRepository personaRepository;
    private final PacienteRepository pacienteRepository;
    private final ExpedienteRepository expedienteRepository;
    private final EpisodioRepository episodioRepository;
    private final PersonaResponsableRepository responsableRepository;
    private final ReferenciaRepository referenciaRepository;
    private final UsuarioRepository usuarioRepository;
        private final parentescoRepository parentescoRepository;
        private final institucionRepository institucionRepository;
        private final camasRepository camasRepository;
        private final ReferenciaArchivoStorage referenciaArchivoStorage;

    @Transactional
    public PacienteDTOs.Response crear(PacienteDTOs.Request request) {
                return crear(request, LocalDate.now(), null);
        }

        @Transactional
        public PacienteDTOs.Response crear(PacienteDTOs.Request request, MultipartFile archivoReferencia) {
                return crear(request, LocalDate.now(), archivoReferencia);
    }

    PacienteDTOs.Response crear(PacienteDTOs.Request request, LocalDate fechaActual) {
                return crear(request, fechaActual, null);
        }

        private PacienteDTOs.Response crear(
                        PacienteDTOs.Request request, LocalDate fechaActual, MultipartFile archivoReferencia) {
                AtomicReference<String> rutaGuardada = new AtomicReference<>();
                try {
                        return crearTransaccional(request, fechaActual, archivoReferencia, rutaGuardada);
                } catch (RuntimeException ex) {
                        try {
                                referenciaArchivoStorage.eliminar(rutaGuardada.get());
                        } catch (RuntimeException errorLimpieza) {
                                ex.addSuppressed(errorLimpieza);
                        }
                        throw ex;
                }
        }

        private PacienteDTOs.Response crearTransaccional(
                        PacienteDTOs.Request request,
                        LocalDate fechaActual,
                        MultipartFile archivoReferencia,
                        AtomicReference<String> rutaGuardada) {
        if (request == null || request.paciente() == null || request.paciente().persona() == null
                || request.episodio() == null || request.episodio().tipoAtencion() == null) {
            throw new IllegalArgumentException("Los datos del paciente y del episodio son obligatorios");
        }
                if (archivoReferencia != null && request.episodio().referencia() == null) {
                        throw new IllegalArgumentException("El archivo solo se permite cuando se envían datos de referencia");
                }

        PacienteDTOs.PersonaRequest datosPersona = request.paciente().persona();
        PacienteDTOs.EpisodioRequest datosEpisodio = request.episodio();
        String cui = StringNormalizer.normalizarNullable(datosPersona.cui());
        Optional<PersonaEntity> personaExistente = cui == null
                ? Optional.empty()
                : personaRepository.findByCui(cui);
        Optional<PacienteEntity> pacienteExistente = personaExistente.flatMap(
                persona -> pacienteRepository.findByPersonaId(persona.getId()));
        LocalDate fechaNacimiento = personaExistente.map(PersonaEntity::getFechaNacimiento)
                .orElse(datosPersona.fechaNacimiento());

        if (fechaNacimiento == null || fechaNacimiento.isAfter(fechaActual)) {
            throw new IllegalArgumentException("La fecha de nacimiento del paciente no es válida");
        }
        boolean menorDeEdad = fechaNacimiento.plusYears(18).isAfter(fechaActual);
        boolean requiereResponsable = datosEpisodio.tipoAtencion() == TipoAtencion.HOSPITALIZACION
                || (datosEpisodio.tipoAtencion() == TipoAtencion.EMERGENCIA && menorDeEdad);
        if (requiereResponsable && datosEpisodio.responsable() == null) {
            throw new IllegalArgumentException("El responsable es obligatorio para esta atención");
        }
                if (datosEpisodio.responsable() != null) {
                        validarDatosPersonaResponsable(datosEpisodio.responsable().persona());
                        String cuiResponsable = StringNormalizer.normalizarTexto(datosEpisodio.responsable().persona().cui());
                        if (cui != null && cui.equals(cuiResponsable)) {
                                throw new IllegalArgumentException("El paciente no puede ser su propio responsable");
                        }
        }

                parentescoEntity parentesco = cargarParentesco(datosEpisodio.responsable());
                institucionEntity institucion = cargarInstitucion(datosEpisodio.referencia());
                camasEntity cama = cargarCama(request.paciente().camaId(), pacienteExistente.orElse(null));

        UsuarioEntity medico = usuarioRepository.findById(datosEpisodio.medicoId())
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        PersonaEntity persona = personaExistente.orElseGet(() -> guardarPersona(datosPersona));
        PacienteEntity paciente = pacienteExistente.orElseGet(() -> pacienteRepository.save(PacienteEntity.builder()
                .persona(persona)
                .estadoCivil(request.paciente().estadoCivil())
                .direccion(StringNormalizer.normalizarNullable(request.paciente().direccion()))
                .ocupacion(StringNormalizer.normalizarNullable(request.paciente().ocupacion()))
                .build()));
        asignarCama(cama, paciente);
        ExpedienteEntity expediente = obtenerOCrearExpediente(paciente);

        PersonaResponsableEntity responsable = guardarResponsable(datosEpisodio.responsable(), parentesco);
        ReferenciaEntity referencia = guardarReferencia(
                datosEpisodio.referencia(), institucion, archivoReferencia, expediente.getId(), rutaGuardada);
        EpisodioEntity episodio = episodioRepository.save(EpisodioEntity.builder()
                .tipoAtencion(datosEpisodio.tipoAtencion())
                .descripcion(StringNormalizer.normalizarTexto(datosEpisodio.descripcion()))
                .paciente(paciente)
                .expediente(expediente)
                .medico(medico)
                .personaResponsable(responsable)
                .referencia(referencia)
                .build());

        return new PacienteDTOs.Response(paciente.getId(), expediente.getId(), episodio.getId(),
                responsable != null ? responsable.getId() : null,
                referencia != null ? referencia.getId() : null, pacienteExistente.isPresent());
    }

        private camasEntity cargarCama(Long camaId, PacienteEntity pacienteExistente) {
                if (camaId == null) {
                        return null;
                }
                camasEntity cama = camasRepository.findById(camaId)
                                .orElseThrow(() -> new IllegalArgumentException("Cama no encontrada con el ID: " + camaId));
                boolean yaAsignadaAlPaciente = pacienteExistente != null && cama.getPaciente() != null
                                && cama.getPaciente().getId().equals(pacienteExistente.getId());
                if (!cama.isActivo()) {
                        throw new IllegalArgumentException("La cama está inactiva");
                }
                if (cama.getPaciente() != null && !yaAsignadaAlPaciente) {
                        throw new IllegalArgumentException("La cama ya está asignada a otro paciente");
                }
                if (cama.getEstado() != EstadoCama.DISPONIBLE && !yaAsignadaAlPaciente) {
                        throw new IllegalArgumentException("La cama no está disponible");
                }
                return cama;
        }

        private void asignarCama(camasEntity cama, PacienteEntity paciente) {
                if (cama == null) {
                        return;
                }
                cama.setPaciente(paciente);
                cama.setEstado(EstadoCama.OCUPADA);
                camasRepository.save(cama);
        }

    @Transactional(readOnly = true)
    public List<PacienteDTOs.ListadoResponse> listar(Boolean estado) {
        boolean estadoFiltro = estado == null || estado;
        List<PacienteEntity> pacientes = pacienteRepository.findByEstadoOrderByIdAsc(estadoFiltro);
        if (pacientes.isEmpty()) {
            return List.of();
        }

        List<Long> pacienteIds = pacientes.stream().map(PacienteEntity::getId).toList();
        Map<Long, String> codigosPorPaciente = new HashMap<>();
        expedienteRepository.findByPaciente_IdIn(pacienteIds).forEach(expediente ->
                codigosPorPaciente.put(expediente.getPaciente().getId(), expediente.getCodigo()));

        Map<Long, TipoAtencion> tratamientoPorPaciente = new HashMap<>();
        episodioRepository.findByPaciente_IdInOrderByIdDesc(pacienteIds).forEach(episodio ->
                tratamientoPorPaciente.putIfAbsent(episodio.getPaciente().getId(), episodio.getTipoAtencion()));

        return pacientes.stream()
                .map(paciente -> new PacienteDTOs.ListadoResponse(
                        codigosPorPaciente.get(paciente.getId()),
                        paciente.getPersona().getNombreCompleto(),
                        paciente.getPersona().getTelefono(),
                        tratamientoPorPaciente.get(paciente.getId()),
                        paciente.isEstado()))
                .toList();
    }

    private ExpedienteEntity obtenerOCrearExpediente(PacienteEntity paciente) {
        ExpedienteEntity expediente = expedienteRepository.findByPacienteId(paciente.getId())
                .orElseGet(() -> expedienteRepository.save(ExpedienteEntity.builder().paciente(paciente).build()));
        expediente.actualizarCodigo();
        return expedienteRepository.save(expediente);
    }

    private PersonaEntity guardarPersona(PacienteDTOs.PersonaRequest datos) {
        PersonaEntity persona = PersonaEntity.builder()
                                .cui(StringNormalizer.normalizarNullable(datos.cui()))
                .sexo(datos.sexo())
                .fechaNacimiento(datos.fechaNacimiento())
                .telefono(StringNormalizer.normalizarNullable(datos.telefono()))
                .email(StringNormalizer.normalizarNullable(datos.email()))
                .build();
        persona.asignarNombres(datos.nombres(), datos.apellidos());
        return personaRepository.save(persona);
    }

        private void validarDatosPersonaResponsable(PacienteDTOs.PersonaRequest datos) {
                if (datos == null) {
                        throw new IllegalArgumentException("Los datos personales del responsable son obligatorios");
                }
                String cui = StringNormalizer.normalizarNullable(datos.cui());
                if (cui == null || !cui.matches("^[0-9]{13}$")) {
                        throw new IllegalArgumentException("El CUI del responsable es obligatorio y debe tener 13 dígitos");
                }
                String telefono = StringNormalizer.normalizarNullable(datos.telefono());
                if (telefono == null || !telefono.matches("^[0-9]{8,15}$")) {
                        throw new IllegalArgumentException("El teléfono del responsable es obligatorio y debe tener entre 8 y 15 dígitos");
                }
        }

        private parentescoEntity cargarParentesco(PacienteDTOs.ResponsableRequest datos) {
        if (datos == null) {
            return null;
        }
                if (datos.parentescoId() == null || datos.parentescoId() <= 0) {
                        throw new IllegalArgumentException("El parentesco es obligatorio");
                }
                return parentescoRepository.findById(datos.parentescoId())
                                .filter(parentescoEntity::isEstado)
                                .orElseThrow(() -> new IllegalArgumentException("Parentesco no encontrado o inactivo"));
        }

        private PersonaResponsableEntity guardarResponsable(
                        PacienteDTOs.ResponsableRequest datos, parentescoEntity parentesco) {
                if (datos == null) {
                        return null;
                }
        String cui = StringNormalizer.normalizarTexto(datos.persona().cui());
        PersonaEntity persona = personaRepository.findByCui(cui)
                .orElseGet(() -> guardarPersona(datos.persona()));
        return responsableRepository.save(PersonaResponsableEntity.builder()
                .persona(persona)
                .parentesco(parentesco)
                .direccion(StringNormalizer.normalizarNullable(datos.direccion()))
                .build());
    }

        private institucionEntity cargarInstitucion(PacienteDTOs.ReferenciaRequest datos) {
                if (datos == null) {
                        return null;
                }
                return institucionRepository.findById(datos.institucionId())
                                .filter(institucionEntity::isEstado)
                                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada o inactiva"));
        }

        private ReferenciaEntity guardarReferencia(
                        PacienteDTOs.ReferenciaRequest datos,
                        institucionEntity institucion,
                        MultipartFile archivo,
                        Long expedienteId,
                        AtomicReference<String> rutaGuardada) {
        if (datos == null) {
            return null;
        }
                String rutaDocumento = referenciaArchivoStorage.guardar(archivo, expedienteId);
                rutaGuardada.set(rutaDocumento);
        return referenciaRepository.save(ReferenciaEntity.builder()
                        .institucion(institucion)
                .motivoReferencia(StringNormalizer.normalizarTexto(datos.motivoReferencia()))
                                .urlDocumento(rutaDocumento)
                .build());
    }
}