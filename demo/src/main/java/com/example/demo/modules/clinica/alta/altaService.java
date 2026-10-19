package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoEntity;
import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoRepository;
import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.pacientes.institucion.institucionEntity;
import com.example.demo.modules.pacientes.institucion.institucionRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class altaService {

    private final altaRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final condicionEgresoRepository condicionEgresoRepository;
    private final institucionRepository institucionRepository;

    @Transactional
    public altaDTOs.Response crear(altaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        if (request.tipoAlta() == null) {
            throw new IllegalArgumentException("El tipo de alta es obligatorio");
        }

        EpisodioEntity episodio = buscarEpisodio(request.episodioId());
        if (!episodio.isEstado()) {
            throw new IllegalArgumentException("El episodio ya está cerrado");
        }
        if (repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                episodio.getId(), EstadoAlta.VIGENTE).isPresent()) {
            throw new IllegalArgumentException("El episodio ya tiene un alta registrada");
        }

        UsuarioEntity usuario = buscarUsuario(request.usuarioId());
        validarSeccion(request.tipoAlta(), request);

        altaEntity entity = altaEntity.builder()
                .tipoAlta(request.tipoAlta())
                .diagnosticoEgreso(validarTexto(request.diagnosticoEgreso(),
                        "El diagnóstico de egreso es obligatorio"))
                .condicionEgreso(buscarCondicion(request.condicionEgresoId()))
                .observacionEgreso(StringNormalizer.normalizarNullable(request.observacionEgreso()))
                .fechaHoraEgreso(LocalDateTime.now())
                .episodio(episodio)
                .usuario(usuario)
                .estado(EstadoAlta.VIGENTE)
                .responsableRetiro(esRetiro(request.tipoAlta())
                        ? request.responsableRetiro()
                        : null)
                .motivoRetiro(esRetiro(request.tipoAlta())
                        ? StringNormalizer.normalizarNullable(request.motivoRetiro())
                        : null)
                .riesgosRetiro(esRetiro(request.tipoAlta())
                        ? normalizarRiesgos(request.riesgosRetiro())
                        : new ArrayList<>())
                .institucion(esReferencia(request.tipoAlta())
                        ? buscarInstitucion(request.institucionId())
                        : null)
                .motivoReferencia(esReferencia(request.tipoAlta())
                        ? StringNormalizer.normalizarNullable(request.motivoReferencia())
                        : null)
                .indicacionesTraslado(esReferencia(request.tipoAlta())
                        ? StringNormalizer.normalizarNullable(request.indicacionesTraslado())
                        : null)
                .tratamientos(mapearTratamientos(request.tratamientos()))
                .build();

        altaEntity guardada = repository.save(entity);

        episodio.setEstado(false);
        episodioRepository.save(episodio);

        return toResponse(guardada);
    }

    @Transactional
    public altaDTOs.Response actualizar(Long id, altaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        if (request.tipoAlta() == null) {
            throw new IllegalArgumentException("El tipo de alta es obligatorio");
        }

        altaEntity entity = buscarEntidad(id);
        if (entity.getEstado() != EstadoAlta.VIGENTE) {
            throw new IllegalArgumentException("El alta no está vigente");
        }

        UsuarioEntity usuario = buscarUsuario(request.usuarioId());
        validarSeccion(request.tipoAlta(), request);

        entity.setTipoAlta(request.tipoAlta());
        entity.setDiagnosticoEgreso(validarTexto(request.diagnosticoEgreso(),
                "El diagnóstico de egreso es obligatorio"));
        entity.setCondicionEgreso(buscarCondicion(request.condicionEgresoId()));
        entity.setObservacionEgreso(StringNormalizer.normalizarNullable(request.observacionEgreso()));
        entity.setUsuario(usuario);
        entity.setResponsableRetiro(esRetiro(request.tipoAlta())
                ? request.responsableRetiro()
                : null);
        entity.setMotivoRetiro(esRetiro(request.tipoAlta())
                ? StringNormalizer.normalizarNullable(request.motivoRetiro())
                : null);
        entity.setRiesgosRetiro(esRetiro(request.tipoAlta())
                ? normalizarRiesgos(request.riesgosRetiro())
                : new ArrayList<>());
        entity.setInstitucion(esReferencia(request.tipoAlta())
                ? buscarInstitucion(request.institucionId())
                : null);
        entity.setMotivoReferencia(esReferencia(request.tipoAlta())
                ? StringNormalizer.normalizarNullable(request.motivoReferencia())
                : null);
        entity.setIndicacionesTraslado(esReferencia(request.tipoAlta())
                ? StringNormalizer.normalizarNullable(request.indicacionesTraslado())
                : null);
        entity.setTratamientos(mapearTratamientos(request.tratamientos()));

        altaEntity guardada = repository.save(entity);

        entity.getEpisodio().setEstado(false);
        episodioRepository.save(entity.getEpisodio());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public altaDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<altaDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                        episodioId, EstadoAlta.VIGENTE)
                .map(this::toResponse)
                .map(List::of)
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<altaDTOs.RecienteResponse> recientes(int horas) {
        if (horas <= 0) {
            throw new IllegalArgumentException("Las horas deben ser mayores a 0");
        }

        LocalDateTime desde = LocalDateTime.now().minusHours(horas);
        return repository.findByEstadoAndFechaHoraEgresoGreaterThanEqualOrderByFechaHoraEgresoDesc(
                        EstadoAlta.VIGENTE, desde).stream()
                .map(this::toReciente)
                .toList();
    }

    @Transactional
    public void anular(Long id) {
        altaEntity entity = buscarEntidad(id);
        if (entity.getEstado() == EstadoAlta.ANULADA) {
            return;
        }

        EpisodioEntity episodio = entity.getEpisodio();
        Long pacienteId = episodio.getPaciente().getId();
        if (episodioRepository.existsByPaciente_IdAndEstadoTrue(pacienteId)) {
            throw new IllegalArgumentException(
                    "El paciente ya tiene otro episodio activo; no se puede reabrir este episodio");
        }

        entity.setEstado(EstadoAlta.ANULADA);
        repository.save(entity);

        episodio.setEstado(true);
        episodioRepository.save(episodio);
    }

    private void validarSeccion(TipoAlta tipo, altaDTOs.Request request) {
        if (tipo == TipoAlta.RETIRO_VOLUNTARIO) {
            if (request.responsableRetiro() == null) {
                throw new IllegalArgumentException("El solicitante del retiro es obligatorio");
            }
            if (StringNormalizer.normalizarTexto(request.motivoRetiro()).isBlank()) {
                throw new IllegalArgumentException("El motivo del retiro es obligatorio");
            }
        }

        if (tipo == TipoAlta.REFERENCIA_TRASLADO) {
            if (request.institucionId() == null) {
                throw new IllegalArgumentException("La institución de referencia es obligatoria");
            }
            if (StringNormalizer.normalizarTexto(request.motivoReferencia()).isBlank()) {
                throw new IllegalArgumentException("El motivo de referencia es obligatorio");
            }
        }
    }

    private List<altaTratamientoEmbeddable> mapearTratamientos(
            List<altaDTOs.TratamientoRequest> tratamientos) {
        List<altaTratamientoEmbeddable> resultado = new ArrayList<>();
        if (tratamientos == null) {
            return resultado;
        }

        for (altaDTOs.TratamientoRequest tratamiento : tratamientos) {
            if (tratamiento == null) {
                continue;
            }
            if (tratamiento.tipo() == null) {
                throw new IllegalArgumentException("El tipo de tratamiento es obligatorio");
            }

            String nombre = validarTexto(tratamiento.nombre(),
                    "El nombre del tratamiento es obligatorio");
            if (tratamiento.frecuencia() == null || tratamiento.frecuencia() <= 0) {
                throw new IllegalArgumentException("La frecuencia debe ser mayor a 0");
            }
            if (tratamiento.duracionTipo() == null) {
                throw new IllegalArgumentException("El tipo de duración es obligatorio");
            }

            Integer cantidad = null;
            Long medicamentoLogId = null;
            String medicamentoLibre = null;
            if (tratamiento.tipo() == TipoTratamiento.MEDICAMENTO) {
                if (tratamiento.cantidad() == null || tratamiento.cantidad() <= 0) {
                    throw new IllegalArgumentException(
                            "La cantidad del medicamento debe ser mayor a 0");
                }
                cantidad = tratamiento.cantidad();
                medicamentoLogId = tratamiento.medicamentoLogId();
                medicamentoLibre = StringNormalizer.normalizarNullable(
                        tratamiento.medicamentoLibre());
                if (medicamentoLogId != null) {
                    medicamentoLibre = null;
                } else if (medicamentoLibre == null) {
                    throw new IllegalArgumentException("El medicamento es obligatorio");
                }
            }

            resultado.add(altaTratamientoEmbeddable.builder()
                    .tipo(tratamiento.tipo())
                    .nombre(nombre)
                    .medicamentoLogId(medicamentoLogId)
                    .medicamentoLibre(medicamentoLibre)
                    .cantidad(cantidad)
                    .unidadMedida(StringNormalizer.normalizarNullable(tratamiento.unidadMedida()))
                    .frecuencia(tratamiento.frecuencia())
                    .duracionTipo(tratamiento.duracionTipo())
                    .duracion(validarDuracion(tratamiento.duracionTipo(),
                            tratamiento.duracion()))
                    .observaciones(StringNormalizer.normalizarNullable(
                            tratamiento.observaciones()))
                    .build());
        }

        return resultado;
    }

    private Integer validarDuracion(TipoDuracion tipo, Integer duracion) {
        if (tipo == TipoDuracion.DIAS) {
            if (duracion == null || duracion <= 0) {
                throw new IllegalArgumentException(
                        "La duración es obligatoria cuando el tipo es DIAS");
            }
            return duracion;
        }
        return null;
    }

    private List<String> normalizarRiesgos(List<String> riesgos) {
        List<String> resultado = new ArrayList<>();
        if (riesgos == null) {
            return resultado;
        }

        Set<String> vistos = new HashSet<>();
        for (String riesgo : riesgos) {
            String normalizado = StringNormalizer.normalizarTexto(riesgo);
            if (normalizado.isBlank()) {
                continue;
            }
            if (vistos.add(normalizado.toLowerCase(Locale.ROOT))) {
                resultado.add(normalizado);
            }
        }
        return resultado;
    }

    private boolean esRetiro(TipoAlta tipo) {
        return tipo == TipoAlta.RETIRO_VOLUNTARIO;
    }

    private boolean esReferencia(TipoAlta tipo) {
        return tipo == TipoAlta.REFERENCIA_TRASLADO;
    }

    private altaEntity buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alta no encontrada con el ID: " + id));
    }

    private EpisodioEntity buscarEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        return episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episodio no encontrado con el ID: " + episodioId));
    }

    private UsuarioEntity buscarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + usuarioId));
    }

    private condicionEgresoEntity buscarCondicion(Long condicionId) {
        if (condicionId == null) {
            return null;
        }

        return condicionEgresoRepository.findById(condicionId)
                .filter(condicionEgresoEntity::isEstado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Condición de egreso no encontrada o inactiva"));
    }

    private institucionEntity buscarInstitucion(Long institucionId) {
        if (institucionId == null) {
            throw new IllegalArgumentException("La institución de referencia es obligatoria");
        }

        return institucionRepository.findById(institucionId)
                .filter(institucionEntity::isEstado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Institución no encontrada o inactiva"));
    }

    private String validarTexto(String valor, String mensaje) {
        String normalizado = StringNormalizer.normalizarTexto(valor);
        if (normalizado.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return normalizado;
    }

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        String nombreCompleto = usuario.getPersona() != null
                ? usuario.getPersona().getNombreCompleto()
                : "";
        return nombreCompleto.isBlank() ? usuario.getUsername() : nombreCompleto;
    }

    private altaDTOs.RecienteResponse toReciente(altaEntity entity) {
        EpisodioEntity episodio = entity.getEpisodio();
        PacienteEntity paciente = episodio.getPaciente();
        PersonaEntity persona = paciente != null ? paciente.getPersona() : null;
        condicionEgresoEntity condicion = entity.getCondicionEgreso();
        institucionEntity institucion = entity.getInstitucion();

        return new altaDTOs.RecienteResponse(
                entity.getId(),
                entity.getTipoAlta(),
                entity.getFechaHoraEgreso(),
                entity.getDiagnosticoEgreso(),
                condicion != null ? condicion.getNombre() : null,
                entity.getObservacionEgreso(),
                entity.getResponsableRetiro(),
                entity.getMotivoRetiro(),
                List.copyOf(entity.getRiesgosRetiro()),
                institucion != null ? institucion.getNombre() : null,
                entity.getMotivoReferencia(),
                entity.getIndicacionesTraslado(),
                episodio.getId(),
                paciente != null ? paciente.getId() : null,
                episodio.getExpediente() != null ? episodio.getExpediente().getCodigo() : null,
                persona != null ? persona.getNombreCompleto() : "",
                persona != null ? persona.getCui() : null,
                persona != null ? persona.getSexo() : null,
                persona != null ? persona.getTelefono() : null,
                episodio.getTipoAtencion(),
                edad(persona));
    }

    private Integer edad(PersonaEntity persona) {
        if (persona == null || persona.getFechaNacimiento() == null) {
            return null;
        }
        return Period.between(persona.getFechaNacimiento(), LocalDate.now()).getYears();
    }

    private altaDTOs.Response toResponse(altaEntity entity) {
        condicionEgresoEntity condicion = entity.getCondicionEgreso();
        institucionEntity institucion = entity.getInstitucion();

        return new altaDTOs.Response(
                entity.getId(),
                entity.getTipoAlta(),
                entity.getDiagnosticoEgreso(),
                condicion != null ? condicion.getId() : null,
                condicion != null ? condicion.getNombre() : null,
                entity.getObservacionEgreso(),
                entity.getFechaHoraEgreso(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getEstado(),
                entity.getResponsableRetiro(),
                entity.getMotivoRetiro(),
                List.copyOf(entity.getRiesgosRetiro()),
                institucion != null ? institucion.getId() : null,
                institucion != null ? institucion.getNombre() : null,
                entity.getMotivoReferencia(),
                entity.getIndicacionesTraslado(),
                entity.getTratamientos().stream()
                        .map(tratamiento -> new altaDTOs.TratamientoResponse(
                                tratamiento.getTipo(),
                                tratamiento.getNombre(),
                                tratamiento.getCantidad(),
                                tratamiento.getUnidadMedida(),
                                tratamiento.getFrecuencia(),
                                tratamiento.getDuracionTipo(),
                                tratamiento.getDuracion(),
                                tratamiento.getObservaciones(),
                                tratamiento.getMedicamentoLogId(),
                                tratamiento.getMedicamentoLibre()))
                        .toList());
    }
}
