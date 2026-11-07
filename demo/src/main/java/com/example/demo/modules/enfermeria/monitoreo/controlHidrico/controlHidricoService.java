package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.EstadoEjecucionCuidado;
import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.ejecucionCuidadoDTOs;
import com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado.ejecucionCuidadoService;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioNombreCorto;
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
public class controlHidricoService {

    private final controlHidricoRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ejecucionCuidadoService ejecucionCuidadoService;
    private final RegistroAccesoService registroAcceso;

    @Transactional
    public controlHidricoDTOs.Response crear(controlHidricoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        controlHidricoEntity entity = controlHidricoEntity.builder()
                .tipo(validarTipo(request.tipo()))
                .via(validarVia(request.via()))
                .cantidad(validarCantidad(request.cantidad()))
                .descripcion(validarDescripcion(request.descripcion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(autor(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        controlHidricoEntity guardado = repository.save(entity);
        registrarEjecucionControl(request, guardado);
        return toResponse(guardado);
    }

    /** Vincula el movimiento con la atención indicada y marca la ejecución como realizada. */
    private void registrarEjecucionControl(controlHidricoDTOs.Request request, controlHidricoEntity entity) {
        if (request.cuidadoId() == null) return;

        LocalDateTime programada = request.fechaHoraProgramada() != null
                ? request.fechaHoraProgramada()
                : LocalDateTime.now();

        ejecucionCuidadoService.crear(new ejecucionCuidadoDTOs.Request(
                request.cuidadoId(),
                programada,
                EstadoEjecucionCuidado.REALIZADO,
                entity.getDescripcion(),
                request.usuarioId(),
                null,
                null,
                entity.getId()));
    }

    @Transactional
    public controlHidricoDTOs.Response actualizar(Long id, controlHidricoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        controlHidricoEntity entity = buscar(id);
        registroAcceso.validarEdicion(entity.getUsuario());

        UsuarioEntity autorActualizado = registroAcceso.autorParaEdicion(entity.getUsuario(), request.usuarioId());
        entity.setUsuario(autorActualizado != null ? autorActualizado : buscarUsuario(request.usuarioId()));

        entity.setTipo(validarTipo(request.tipo()));
        entity.setVia(validarVia(request.via()));
        entity.setCantidad(validarCantidad(request.cantidad()));
        entity.setDescripcion(validarDescripcion(request.descripcion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public controlHidricoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<controlHidricoDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }
        registroAcceso.validarAccesoEpisodio(episodioId);

        return repository.findByEpisodio_IdOrderByFechaDesc(episodioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        controlHidricoEntity entity = buscar(id);
        registroAcceso.validarEdicion(entity.getUsuario());
        entity.setEliminado(true);
        repository.save(entity);
    }

    private controlHidricoEntity buscar(Long id) {
        controlHidricoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de control hídrico no encontrado con el ID: " + id));
        registroAcceso.validarAccesoEpisodio(entity.getEpisodio());
        return entity;
    }

    private TipoRegistroHidrico validarTipo(TipoRegistroHidrico tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de registro es obligatorio");
        }
        return tipo;
    }

    private String validarVia(String via) {
        String normalizada = StringNormalizer.normalizarTexto(via);
        if (normalizada.isBlank()) {
            throw new IllegalArgumentException("La vía es obligatoria");
        }
        return normalizada;
    }

    private Integer validarCantidad(Integer cantidad) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        return cantidad;
    }

    private String validarDescripcion(String descripcion) {
        String normalizada = StringNormalizer.normalizarTexto(descripcion);
        if (normalizada.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        return normalizada;
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

    private controlHidricoDTOs.Response toResponse(controlHidricoEntity entity) {
        return new controlHidricoDTOs.Response(
                entity.getId(),
                entity.getTipo(),
                entity.getVia(),
                entity.getCantidad(),
                entity.getDescripcion(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                UsuarioNombreCorto.formatear(entity.getUsuario()),
                entity.getFecha(),
                entity.isEliminado());
    }
}
