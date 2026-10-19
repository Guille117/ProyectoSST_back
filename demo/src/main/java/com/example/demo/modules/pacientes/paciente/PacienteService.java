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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        private final DocumentoEpisodioService documentoEpisodioService;

    @Transactional
    public PacienteDTOs.Response crear(PacienteDTOs.Request request) {
        return crear(request, LocalDate.now(), null, null, null, null);
    }

    @Transactional
    public PacienteDTOs.Response crear(PacienteDTOs.Request request, MultipartFile archivoReferencia) {
        return crear(request, LocalDate.now(), archivoReferencia, null, null, null);
    }

    @Transactional
    public PacienteDTOs.Response crear(
            PacienteDTOs.Request request,
            MultipartFile archivoReferencia,
            List<MultipartFile> referenciaArchivos,
            List<MultipartFile> autorizacionArchivos,
            List<MultipartFile> responsabilidadArchivos) {
        return crear(request, LocalDate.now(), archivoReferencia, referenciaArchivos,
                autorizacionArchivos, responsabilidadArchivos);
    }

    PacienteDTOs.Response crear(PacienteDTOs.Request request, LocalDate fechaActual) {
        return crear(request, fechaActual, null, null, null, null);
    }

    private PacienteDTOs.Response crear(
            PacienteDTOs.Request request,
            LocalDate fechaActual,
            MultipartFile archivoReferencia,
            List<MultipartFile> referenciaArchivos,
            List<MultipartFile> autorizacionArchivos,
            List<MultipartFile> responsabilidadArchivos) {
        return crearTransaccional(request, fechaActual, archivoReferencia, referenciaArchivos,
                autorizacionArchivos, responsabilidadArchivos);
    }

    private PacienteDTOs.Response crearTransaccional(
            PacienteDTOs.Request request,
            LocalDate fechaActual,
            MultipartFile archivoReferencia,
            List<MultipartFile> referenciaArchivos,
            List<MultipartFile> autorizacionArchivos,
            List<MultipartFile> responsabilidadArchivos) {
        if (request == null || request.paciente() == null || request.paciente().persona() == null
                || request.episodio() == null || request.episodio().tipoAtencion() == null) {
            throw new IllegalArgumentException("Los datos del paciente y del episodio son obligatorios");
        }
        List<MultipartFile> referencias = unirArchivos(archivoReferencia, referenciaArchivos);
        if (!referencias.isEmpty() && request.episodio().referencia() == null) {
            throw new IllegalArgumentException("El archivo solo se permite cuando se envían datos de referencia");
        }

        PacienteDTOs.PersonaRequest datosPersona = request.paciente().persona();
        PacienteDTOs.EpisodioRequest datosEpisodio = request.episodio();
        String cui = StringNormalizer.normalizarNullable(datosPersona.cui());
        Long pacienteId = request.paciente().pacienteId();

        PersonaEntity persona;
        PacienteEntity paciente;
        LocalDate fechaNacimiento;

        if (pacienteId != null) {
            paciente = pacienteRepository.findById(pacienteId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Paciente no encontrado con el ID: " + pacienteId));
            persona = paciente.getPersona();
            fechaNacimiento = datosPersona.fechaNacimiento();
        } else {
            Optional<PersonaEntity> personaExistente = cui == null
                    ? Optional.empty()
                    : personaRepository.findByCui(cui);
            Optional<PacienteEntity> pacienteEncontrado = personaExistente.flatMap(
                    personaEncontrada -> pacienteRepository.findByPersonaId(personaEncontrada.getId()));
            persona = personaExistente.orElse(null);
            paciente = pacienteEncontrado.orElse(null);
            fechaNacimiento = personaExistente.map(PersonaEntity::getFechaNacimiento)
                    .orElse(datosPersona.fechaNacimiento());
        }

        if (fechaNacimiento == null || fechaNacimiento.isAfter(fechaActual)) {
            throw new IllegalArgumentException("La fecha de nacimiento del paciente no es válida");
        }

        if (paciente != null && episodioRepository.existsByPaciente_IdAndEstadoTrue(paciente.getId())) {
            throw new IllegalArgumentException(
                    "El paciente ya tiene un episodio activo. Debe registrar el alta antes de crear otro.");
        }

        boolean pacienteExistente = paciente != null;
        boolean menorDeEdad = fechaNacimiento.plusYears(18).isAfter(fechaActual);
        boolean requiereResponsable = datosEpisodio.tipoAtencion() == TipoAtencion.HOSPITALIZACION
                || (datosEpisodio.tipoAtencion() == TipoAtencion.EMERGENCIA && menorDeEdad);
        if (requiereResponsable && datosEpisodio.responsable() == null) {
            throw new IllegalArgumentException("El responsable es obligatorio para esta atención");
        }
        if (datosEpisodio.responsable() != null) {
            validarDatosPersonaResponsable(datosEpisodio.responsable().persona());
            String cuiResponsable = StringNormalizer.normalizarTexto(datosEpisodio.responsable().persona().cui());
            String cuiPaciente = pacienteId != null
                    ? cui
                    : (persona != null ? persona.getCui() : cui);
            if (cuiPaciente != null && cuiPaciente.equals(cuiResponsable)) {
                throw new IllegalArgumentException("El paciente no puede ser su propio responsable");
            }
        }

        parentescoEntity parentesco = cargarParentesco(datosEpisodio.responsable());
        institucionEntity institucion = cargarInstitucion(datosEpisodio.referencia());
        camasEntity cama = cargarCama(request.paciente().camaId(), paciente);

        UsuarioEntity medico = usuarioRepository.findById(datosEpisodio.medicoId())
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        if (pacienteId != null) {
            if (cui != null) {
                Optional<PersonaEntity> otraPersona = personaRepository.findByCui(cui);
                if (otraPersona.isPresent() && !otraPersona.get().getId().equals(persona.getId())) {
                    throw new IllegalArgumentException("El CUI ya está registrado por otra persona");
                }
            }

            persona.setCui(cui);
            persona.setSexo(datosPersona.sexo());
            persona.setFechaNacimiento(datosPersona.fechaNacimiento());
            persona.setTelefono(StringNormalizer.normalizarNullable(datosPersona.telefono()));
            persona.setEmail(StringNormalizer.normalizarNullable(datosPersona.email()));
            persona.asignarNombres(datosPersona.nombres(), datosPersona.apellidos());
            personaRepository.save(persona);

            paciente.setEstadoCivil(request.paciente().estadoCivil());
            paciente.setDireccion(StringNormalizer.normalizarNullable(request.paciente().direccion()));
            paciente.setOcupacion(StringNormalizer.normalizarNullable(request.paciente().ocupacion()));
            paciente.setEstado(true);
            pacienteRepository.save(paciente);
        } else if (paciente != null) {
            // Reutilización por CUI: se conservan los datos existentes y se reactiva el paciente.
            if (!paciente.isEstado()) {
                paciente.setEstado(true);
                pacienteRepository.save(paciente);
            }
        } else {
            persona = persona != null ? persona : guardarPersona(datosPersona);
            paciente = pacienteRepository.save(PacienteEntity.builder()
                    .persona(persona)
                    .estadoCivil(request.paciente().estadoCivil())
                    .direccion(StringNormalizer.normalizarNullable(request.paciente().direccion()))
                    .ocupacion(StringNormalizer.normalizarNullable(request.paciente().ocupacion()))
                    .estado(true)
                    .build());
        }
        asignarCama(cama, paciente);
        ExpedienteEntity expediente = obtenerOCrearExpediente(paciente);

        PersonaResponsableEntity responsable = guardarResponsable(datosEpisodio.responsable(), parentesco);
        ReferenciaEntity referencia = guardarReferencia(datosEpisodio.referencia(), institucion);
        EpisodioEntity episodio = episodioRepository.save(EpisodioEntity.builder()
                .tipoAtencion(datosEpisodio.tipoAtencion())
                .descripcion(StringNormalizer.normalizarTexto(datosEpisodio.descripcion()))
                .fechaHoraIngreso(LocalDateTime.now())
                .paciente(paciente)
                .expediente(expediente)
                .medico(medico)
                .personaResponsable(responsable)
                .referencia(referencia)
                .build());

        Map<TipoDocumentoEpisodio, List<MultipartFile>> documentos = new HashMap<>();
        if (!referencias.isEmpty()) {
            documentos.put(TipoDocumentoEpisodio.REFERENCIA, referencias);
        }
        if (autorizacionArchivos != null && !autorizacionArchivos.isEmpty()) {
            documentos.put(TipoDocumentoEpisodio.AUTORIZACION_PROCEDIMIENTO, autorizacionArchivos);
        }
        if (responsabilidadArchivos != null && !responsabilidadArchivos.isEmpty()) {
            documentos.put(TipoDocumentoEpisodio.RESPONSABILIDAD, responsabilidadArchivos);
        }
        if (!documentos.isEmpty()) {
            Long usuarioId = request.usuarioId() != null ? request.usuarioId() : datosEpisodio.medicoId();
            documentoEpisodioService.guardarTodos(episodio, usuarioId, documentos);
        }

        return new PacienteDTOs.Response(paciente.getId(), expediente.getId(), episodio.getId(),
                episodio.getFechaHoraIngreso(),
                responsable != null ? responsable.getId() : null,
                referencia != null ? referencia.getId() : null, pacienteExistente);
    }

    private List<MultipartFile> unirArchivos(MultipartFile archivo, List<MultipartFile> archivos) {
        List<MultipartFile> resultado = new ArrayList<>();
        if (archivo != null) {
            resultado.add(archivo);
        }
        if (archivos != null) {
            archivos.stream().filter(item -> item != null).forEach(resultado::add);
        }
        return resultado;
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

                // Si el paciente ya tenía otra cama, se libera antes de ocupar la nueva.
                camasEntity camaAnterior = camasRepository.findByPaciente_Id(paciente.getId()).orElse(null);
                if (camaAnterior != null && !camaAnterior.getId().equals(cama.getId())) {
                        camaAnterior.setPaciente(null);
                        camaAnterior.setEstado(EstadoCama.DISPONIBLE);
                        camasRepository.save(camaAnterior);
                        // Se confirma la liberación antes de ocupar la nueva para no chocar con la
                        // restricción única de la cama por paciente.
                        camasRepository.flush();
                }

                cama.setPaciente(paciente);
                cama.setEstado(EstadoCama.OCUPADA);
                camasRepository.save(cama);
        }

    @Transactional(readOnly = true)
    public List<PacienteDTOs.ListadoResponse> listar(Boolean estado) {
        boolean estadoFiltro = estado == null || estado;
        return mapearListado(pacienteRepository.findByEstadoOrderByIdAsc(estadoFiltro));
    }

    /** Busca pacientes por nombre o CUI, incluyendo activos y egresados. */
    @Transactional(readOnly = true)
    public List<PacienteDTOs.ListadoResponse> buscar(String texto) {
        String termino = normalizarBusqueda(texto);
        if (termino.isEmpty()) {
            return List.of();
        }

        List<PacienteEntity> coincidencias = pacienteRepository.findAll().stream()
                .filter(paciente -> coincideBusqueda(paciente, termino))
                .toList();
        return mapearListado(coincidencias);
    }

    private boolean coincideBusqueda(PacienteEntity paciente, String termino) {
        PersonaEntity persona = paciente.getPersona();
        return normalizarBusqueda(persona.getNombreCompleto()).contains(termino)
                || normalizarBusqueda(persona.getCui()).contains(termino);
    }

    private String normalizarBusqueda(String valor) {
        if (valor == null) {
            return "";
        }
        return java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(java.util.Locale.ROOT);
    }

    private List<PacienteDTOs.ListadoResponse> mapearListado(List<PacienteEntity> pacientes) {
        if (pacientes.isEmpty()) {
            return List.of();
        }

        List<Long> pacienteIds = pacientes.stream().map(PacienteEntity::getId).toList();
        Map<Long, String> codigosPorPaciente = new HashMap<>();
        expedienteRepository.findByPaciente_IdIn(pacienteIds).forEach(expediente ->
                codigosPorPaciente.put(expediente.getPaciente().getId(), expediente.getCodigo()));

        Map<Long, TipoAtencion> tratamientoPorPaciente = new HashMap<>();
        Map<Long, Long> episodioIdPorPaciente = new HashMap<>();
        Map<Long, Long> ultimoEpisodioIdPorPaciente = new HashMap<>();
        Map<Long, LocalDateTime> fechaIngresoPorPaciente = new HashMap<>();
        episodioRepository.findByPaciente_IdInOrderByIdDesc(pacienteIds).forEach(episodio -> {
                Long pacienteId = episodio.getPaciente().getId();
                tratamientoPorPaciente.putIfAbsent(pacienteId, episodio.getTipoAtencion());
                // El último episodio se conserva aunque esté cerrado, para consultar su historial
                ultimoEpisodioIdPorPaciente.putIfAbsent(pacienteId, episodio.getId());
                // El episodio activo más reciente es el que abre la pantalla de atención médica
                if (episodio.isEstado()) {
                        episodioIdPorPaciente.putIfAbsent(pacienteId, episodio.getId());
                        fechaIngresoPorPaciente.putIfAbsent(pacienteId, episodio.getFechaHoraIngreso());
                }
        });

        return pacientes.stream()
                .map(paciente -> new PacienteDTOs.ListadoResponse(
                        paciente.getId(),
                        codigosPorPaciente.get(paciente.getId()),
                        episodioIdPorPaciente.get(paciente.getId()),
                        ultimoEpisodioIdPorPaciente.get(paciente.getId()),
                        fechaIngresoPorPaciente.get(paciente.getId()),
                        paciente.getPersona().getNombreCompleto(),
                        paciente.getPersona().getCui(),
                        paciente.getPersona().getSexo(),
                        paciente.getPersona().getTelefono(),
                        tratamientoPorPaciente.get(paciente.getId()),
                        paciente.isEstado()))
                .toList();
    }

    /** Detalle del paciente con su episodio activo, cama y responsable. */
    @Transactional(readOnly = true)
    public PacienteDTOs.DetalleResponse obtenerDetalle(Long pacienteId) {
        PacienteEntity paciente = buscarPaciente(pacienteId);
        PersonaEntity persona = paciente.getPersona();
        EpisodioEntity episodio = episodioRepository
                .findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(pacienteId)
                .orElse(null);
        // Para precargar al responsable de un egresado se usa su último episodio, aunque esté cerrado.
        EpisodioEntity episodioConResponsable = episodio != null
                ? episodio
                : episodioRepository.findFirstByPaciente_IdOrderByIdDesc(pacienteId).orElse(null);
        camasEntity cama = camasRepository.findByPaciente_IdAndActivoTrue(pacienteId).orElse(null);
        ExpedienteEntity expediente = expedienteRepository.findByPacienteId(pacienteId).orElse(null);

        return new PacienteDTOs.DetalleResponse(
                paciente.getId(),
                expediente != null ? expediente.getCodigo() : null,
                persona.getNombreCompleto(),
                unirPartes(persona.getPrimerNombre(), persona.getSegundoNombre(), persona.getOtrosNombres()),
                unirPartes(persona.getPrimerApellido(), persona.getSegundoApellido()),
                persona.getCui(),
                persona.getFechaNacimiento(),
                calcularEdad(persona.getFechaNacimiento()),
                persona.getSexo(),
                persona.getTelefono(),
                persona.getEmail(),
                paciente.getEstadoCivil(),
                paciente.getDireccion(),
                paciente.getOcupacion(),
                paciente.isEstado(),
                episodio != null ? episodio.getId() : null,
                episodio != null ? episodio.getTipoAtencion() : null,
                episodio != null ? episodio.getDescripcion() : null,
                episodio != null ? episodio.getFechaHoraIngreso() : null,
                episodio != null && episodio.getMedico() != null ? episodio.getMedico().getId() : null,
                episodio != null && episodio.getMedico() != null && episodio.getMedico().getPersona() != null
                        ? episodio.getMedico().getPersona().getNombreCompleto()
                        : null,
                cama != null ? cama.getId() : null,
                cama != null ? cama.getCodigo() : null,
                responsableDetalle(episodioConResponsable),
                referenciaDetalle(episodioConResponsable));
    }

    /**
     * Actualiza los datos del paciente y, cuando el episodio viene en la solicitud, también los datos
     * de su atención activa. El archivo de referencia es opcional y se registra como documento del
     * episodio.
     */
    @Transactional
    public PacienteDTOs.Response actualizar(
            Long pacienteId, PacienteDTOs.ActualizacionRequest request, MultipartFile archivoReferencia) {
        if (request == null || request.paciente() == null || request.paciente().persona() == null) {
            throw new IllegalArgumentException("Los datos del paciente son obligatorios");
        }

        PacienteEntity paciente = buscarPaciente(pacienteId);
        PersonaEntity persona = paciente.getPersona();
        PacienteDTOs.PersonaRequest datosPersona = request.paciente().persona();

        if (datosPersona.fechaNacimiento() == null || datosPersona.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento del paciente no es válida");
        }

        String cui = StringNormalizer.normalizarNullable(datosPersona.cui());
        if (cui != null) {
            personaRepository.findByCui(cui).ifPresent(otra -> {
                if (!otra.getId().equals(persona.getId())) {
                    throw new IllegalArgumentException("El CUI ya está registrado por otra persona");
                }
            });
        }

        persona.setCui(cui);
        persona.setSexo(datosPersona.sexo());
        persona.setFechaNacimiento(datosPersona.fechaNacimiento());
        persona.setTelefono(StringNormalizer.normalizarNullable(datosPersona.telefono()));
        persona.setEmail(StringNormalizer.normalizarNullable(datosPersona.email()));
        persona.asignarNombres(datosPersona.nombres(), datosPersona.apellidos());
        personaRepository.save(persona);

        paciente.setEstadoCivil(request.paciente().estadoCivil());
        paciente.setDireccion(StringNormalizer.normalizarNullable(request.paciente().direccion()));
        paciente.setOcupacion(StringNormalizer.normalizarNullable(request.paciente().ocupacion()));
        pacienteRepository.save(paciente);

        EpisodioEntity episodio = episodioRepository
                .findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(pacienteId)
                .orElse(null);

        Long responsableId = null;
        Long referenciaId = null;
        if (request.episodio() != null) {
            if (episodio == null) {
                throw new IllegalArgumentException("El paciente no tiene un episodio activo para actualizar");
            }
            responsableId = actualizarEpisodio(paciente, persona, episodio, request, archivoReferencia);
            referenciaId = episodio.getReferencia() != null ? episodio.getReferencia().getId() : null;
        } else if (archivoReferencia != null) {
            throw new IllegalArgumentException("No hay un episodio activo para adjuntar documentos");
        }

        ExpedienteEntity expediente = expedienteRepository.findByPacienteId(pacienteId).orElse(null);
        return new PacienteDTOs.Response(
                pacienteId,
                expediente != null ? expediente.getId() : null,
                episodio != null ? episodio.getId() : null,
                episodio != null ? episodio.getFechaHoraIngreso() : null,
                responsableId,
                referenciaId,
                true);
    }

    /** Actualiza la atención activa y devuelve el identificador del responsable resultante. */
    private Long actualizarEpisodio(
            PacienteEntity paciente,
            PersonaEntity persona,
            EpisodioEntity episodio,
            PacienteDTOs.ActualizacionRequest request,
            MultipartFile archivoReferencia) {
        PacienteDTOs.EpisodioRequest datos = request.episodio();
        if (datos.tipoAtencion() == null) {
            throw new IllegalArgumentException("El tipo de atención es obligatorio");
        }

        boolean menorDeEdad = esMenorDeEdad(persona.getFechaNacimiento());
        boolean requiereResponsable = datos.tipoAtencion() == TipoAtencion.HOSPITALIZACION
                || (datos.tipoAtencion() == TipoAtencion.EMERGENCIA && menorDeEdad);
        if (requiereResponsable && datos.responsable() == null) {
            throw new IllegalArgumentException("El responsable es obligatorio para esta atención");
        }
        if (datos.responsable() != null) {
            validarDatosPersonaResponsable(datos.responsable().persona());
            String cuiResponsable = StringNormalizer.normalizarTexto(datos.responsable().persona().cui());
            if (persona.getCui() != null && persona.getCui().equals(cuiResponsable)) {
                throw new IllegalArgumentException("El paciente no puede ser su propio responsable");
            }
        }

        UsuarioEntity medico = usuarioRepository.findById(datos.medicoId())
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        parentescoEntity parentesco = cargarParentesco(datos.responsable());
        institucionEntity institucion = cargarInstitucion(datos.referencia());

        camasEntity camaSolicitada = datos.tipoAtencion() == TipoAtencion.HOSPITALIZACION
                ? cargarCama(request.paciente().camaId(), paciente)
                : null;
        if (datos.tipoAtencion() == TipoAtencion.HOSPITALIZACION && camaSolicitada == null) {
            throw new IllegalArgumentException("La cama es obligatoria para la hospitalización");
        }

        episodio.setTipoAtencion(datos.tipoAtencion());
        episodio.setDescripcion(StringNormalizer.normalizarTexto(datos.descripcion()));
        episodio.setMedico(medico);
        actualizarResponsable(episodio, datos.responsable(), parentesco);
        actualizarReferencia(episodio, datos.referencia(), institucion);
        episodioRepository.save(episodio);

        actualizarCama(paciente, camaSolicitada);

        if (archivoReferencia != null) {
            Long usuarioId = request.usuarioId() != null ? request.usuarioId() : datos.medicoId();
            documentoEpisodioService.guardarTodos(episodio, usuarioId,
                    Map.of(TipoDocumentoEpisodio.REFERENCIA, List.of(archivoReferencia)));
        }

        return episodio.getPersonaResponsable() != null ? episodio.getPersonaResponsable().getId() : null;
    }

    private void actualizarResponsable(
            EpisodioEntity episodio,
            PacienteDTOs.ResponsableRequest datos,
            parentescoEntity parentesco) {
        PersonaResponsableEntity actual = episodio.getPersonaResponsable();
        if (datos == null) {
            episodio.setPersonaResponsable(null);
            if (actual != null) {
                actual.setEstado(false);
                responsableRepository.save(actual);
            }
            return;
        }

        PersonaEntity personaResponsable = resolverPersonaResponsable(datos.persona(), actual);
        if (actual == null) {
            actual = PersonaResponsableEntity.builder()
                    .persona(personaResponsable)
                    .parentesco(parentesco)
                    .direccion(StringNormalizer.normalizarNullable(datos.direccion()))
                    .estado(true)
                    .build();
        } else {
            actual.setPersona(personaResponsable);
            actual.setParentesco(parentesco);
            actual.setDireccion(StringNormalizer.normalizarNullable(datos.direccion()));
            actual.setEstado(true);
        }
        episodio.setPersonaResponsable(responsableRepository.save(actual));
    }

    private PersonaEntity resolverPersonaResponsable(
            PacienteDTOs.PersonaRequest datos, PersonaResponsableEntity actual) {
        String cui = StringNormalizer.normalizarTexto(datos.cui());
        PersonaEntity personaActual = actual != null ? actual.getPersona() : null;

        if (personaActual != null && cui.equals(personaActual.getCui())) {
            personaActual.setSexo(datos.sexo());
            personaActual.setFechaNacimiento(datos.fechaNacimiento());
            personaActual.setTelefono(StringNormalizer.normalizarNullable(datos.telefono()));
            personaActual.setEmail(StringNormalizer.normalizarNullable(datos.email()));
            personaActual.asignarNombres(datos.nombres(), datos.apellidos());
            return personaRepository.save(personaActual);
        }

        return personaRepository.findByCui(cui).orElseGet(() -> guardarPersona(datos));
    }

    private void actualizarReferencia(
            EpisodioEntity episodio,
            PacienteDTOs.ReferenciaRequest datos,
            institucionEntity institucion) {
        ReferenciaEntity actual = episodio.getReferencia();
        if (datos == null) {
            episodio.setReferencia(null);
            if (actual != null) {
                actual.setEstado(false);
                referenciaRepository.save(actual);
            }
            return;
        }

        if (actual == null) {
            actual = ReferenciaEntity.builder()
                    .institucion(institucion)
                    .motivoReferencia(StringNormalizer.normalizarTexto(datos.motivoReferencia()))
                    .estado(true)
                    .build();
        } else {
            actual.setInstitucion(institucion);
            actual.setMotivoReferencia(StringNormalizer.normalizarTexto(datos.motivoReferencia()));
            actual.setEstado(true);
        }
        episodio.setReferencia(referenciaRepository.save(actual));
    }

    /** Libera la cama anterior y ocupa la nueva cuando cambió. */
    private void actualizarCama(PacienteEntity paciente, camasEntity nueva) {
        camasEntity actual = camasRepository.findByPaciente_Id(paciente.getId()).orElse(null);

        if (actual != null && (nueva == null || !actual.getId().equals(nueva.getId()))) {
            actual.setPaciente(null);
            actual.setEstado(EstadoCama.DISPONIBLE);
            camasRepository.save(actual);
            // Se confirma la liberación antes de ocupar la nueva para no chocar con la restricción
            // única de la cama por paciente.
            camasRepository.flush();
        }

        if (nueva != null && (actual == null || !nueva.getId().equals(actual.getId()))) {
            nueva.setPaciente(paciente);
            nueva.setEstado(EstadoCama.OCUPADA);
            camasRepository.save(nueva);
        }
    }

    private PacienteDTOs.ResponsableDetalle responsableDetalle(EpisodioEntity episodio) {
        if (episodio == null || episodio.getPersonaResponsable() == null) {
            return null;
        }
        PersonaResponsableEntity responsable = episodio.getPersonaResponsable();
        PersonaEntity persona = responsable.getPersona();
        if (persona == null) {
            return null;
        }
        return new PacienteDTOs.ResponsableDetalle(
                responsable.getId(),
                persona.getCui(),
                unirPartes(persona.getPrimerNombre(), persona.getSegundoNombre(), persona.getOtrosNombres()),
                unirPartes(persona.getPrimerApellido(), persona.getSegundoApellido()),
                persona.getFechaNacimiento(),
                persona.getSexo(),
                persona.getTelefono(),
                responsable.getParentesco() != null ? responsable.getParentesco().getId() : null,
                responsable.getParentesco() != null ? responsable.getParentesco().getNombre() : null,
                responsable.getDireccion());
    }

    private PacienteDTOs.ReferenciaDetalle referenciaDetalle(EpisodioEntity episodio) {
        if (episodio == null || episodio.getReferencia() == null) {
            return null;
        }
        ReferenciaEntity referencia = episodio.getReferencia();
        return new PacienteDTOs.ReferenciaDetalle(
                referencia.getId(),
                referencia.getInstitucion() != null ? referencia.getInstitucion().getId() : null,
                referencia.getInstitucion() != null ? referencia.getInstitucion().getNombre() : null,
                referencia.getMotivoReferencia());
    }

    private PacienteEntity buscarPaciente(Long pacienteId) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con el ID: " + pacienteId));
    }

    private boolean esMenorDeEdad(LocalDate fechaNacimiento) {
        return fechaNacimiento != null && fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }

    private Integer calcularEdad(LocalDate fechaNacimiento) {
        return fechaNacimiento == null ? null : java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    private String unirPartes(String... partes) {
        return java.util.stream.Stream.of(partes)
                .filter(parte -> parte != null && !parte.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
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
            PacienteDTOs.ReferenciaRequest datos, institucionEntity institucion) {
        if (datos == null) {
            return null;
        }
        return referenciaRepository.save(ReferenciaEntity.builder()
                .institucion(institucion)
                .motivoReferencia(StringNormalizer.normalizarTexto(datos.motivoReferencia()))
                .build());
    }
}