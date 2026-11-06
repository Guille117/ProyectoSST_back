package com.example.demo.modules.enfermeria.monitoreo.glucosa;

import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.EstadoEjecucionCuidado;
import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.ejecucionCuidadoDTOs;
import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.ejecucionCuidadoService;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.security.RegistroAccesoService;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class glucosaService {

    private final glucosaRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ejecucionCuidadoService ejecucionCuidadoService;
    private final RegistroAccesoService registroAcceso;

    @Transactional
    public glucosaDTOs.Response crear(glucosaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        glucosaEntity entity = glucosaEntity.builder()
                .glucosa(validarGlucosa(request.glucosa()))
                .tipo(validarTipo(request.tipo()))
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(autor(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        glucosaEntity guardado = repository.save(entity);
        registrarEjecucionControl(request, guardado);
        return toResponse(guardado);
    }

    /** Vincula la medición con la atención indicada y marca la ejecución como realizada. */
    private void registrarEjecucionControl(glucosaDTOs.Request request, glucosaEntity entity) {
        if (request.cuidadoId() == null) return;

        LocalDateTime programada = request.fechaHoraProgramada() != null
                ? request.fechaHoraProgramada()
                : LocalDateTime.now();

        ejecucionCuidadoService.crear(new ejecucionCuidadoDTOs.Request(
                request.cuidadoId(),
                programada,
                EstadoEjecucionCuidado.REALIZADO,
                entity.getObservacion(),
                request.usuarioId(),
                null,
                entity.getId(),
                null));
    }

    @Transactional
    public glucosaDTOs.Response actualizar(Long id, glucosaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        glucosaEntity entity = buscar(id);
        registroAcceso.validarEdicion(entity.getUsuario());

        UsuarioEntity autorActualizado = registroAcceso.autorParaEdicion(entity.getUsuario(), request.usuarioId());
        entity.setUsuario(autorActualizado != null ? autorActualizado : buscarUsuario(request.usuarioId()));

        entity.setGlucosa(validarGlucosa(request.glucosa()));
        entity.setTipo(validarTipo(request.tipo()));
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public glucosaDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<glucosaDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }
        registroAcceso.validarAccesoEpisodio(episodioId);

        return repository.findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(episodioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        glucosaEntity entity = buscar(id);
        registroAcceso.validarEdicion(entity.getUsuario());
        entity.setEliminado(true);
        repository.save(entity);
    }

    private glucosaEntity buscar(Long id) {
        glucosaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de glucosa no encontrado con el ID: " + id));
        registroAcceso.validarAccesoEpisodio(entity.getEpisodio());
        return entity;
    }

    private Integer validarGlucosa(Integer glucosa) {
        if (glucosa == null) {
            throw new IllegalArgumentException("La glucosa es obligatoria");
        }
        if (glucosa <= 0) {
            throw new IllegalArgumentException("La glucosa debe ser mayor a 0");
        }
        return glucosa;
    }

    private TipoMedicionGlucosa validarTipo(TipoMedicionGlucosa tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de medición es obligatorio");
        }
        return tipo;
    }

    private EpisodioEntity buscarEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        EpisodioEntity episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado con el ID: " + episodioId));
        registroAcceso.validarAccesoEpisodio(episodio);
        return episodio;
    }

    private UsuarioEntity autor(Long usuarioId) {
        UsuarioEntity autenticado = registroAcceso.autorActual(usuarioId);
        return autenticado != null ? autenticado : buscarUsuario(usuarioId);
    }

    private UsuarioEntity buscarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
    }

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        String nombreCompleto = usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : "";
        return nombreCompleto.isBlank() ? usuario.getUsername() : nombreCompleto;
    }

    private glucosaDTOs.Response toResponse(glucosaEntity entity) {
        return new glucosaDTOs.Response(
                entity.getId(),
                entity.getGlucosa(),
                entity.getTipo(),
                entity.getObservacion(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEliminado());
    }
}
