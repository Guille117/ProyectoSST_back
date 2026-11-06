package com.example.demo.modules.clinica.diagnostico;

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
public class diagnosticoService {

    private final diagnosticoRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAccesoService registroAcceso;

    @Transactional
    public diagnosticoDTOs.Response crear(diagnosticoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        diagnosticoEntity entity = diagnosticoEntity.builder()
                .tipoDiagnostico(validarTipo(request.tipoDiagnostico()))
                .descripcion(validarDescripcion(request.descripcion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(autor(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public diagnosticoDTOs.Response actualizar(Long id, diagnosticoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        diagnosticoEntity entity = buscarEntidad(id);
        registroAcceso.validarEdicion(entity.getUsuario());
        UsuarioEntity autorActualizado = registroAcceso.autorParaEdicion(entity.getUsuario(), request.usuarioId());
        entity.setUsuario(autorActualizado != null ? autorActualizado : buscarUsuario(request.usuarioId()));

        entity.setTipoDiagnostico(validarTipo(request.tipoDiagnostico()));
        entity.setDescripcion(validarDescripcion(request.descripcion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public diagnosticoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<diagnosticoDTOs.Response> obtenerTodos(boolean activo) {
        return repository.findByEstadoOrderByFechaDesc(activo).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<diagnosticoDTOs.Response> buscarPorEpisodio(Long episodioId, boolean activo) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }
        registroAcceso.validarAccesoEpisodio(episodioId);

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, activo).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<diagnosticoDTOs.Response> buscarPorTipo(TipoDiagnostico tipoDiagnostico, boolean activo) {
        if (tipoDiagnostico == null) {
            throw new IllegalArgumentException("El tipo de diagnóstico es obligatorio para la búsqueda");
        }

        return repository.findByTipoDiagnosticoAndEstadoOrderByFechaDesc(tipoDiagnostico, activo).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public diagnosticoDTOs.Response cambiarEstado(Long id) {
        diagnosticoEntity entity = buscarEntidad(id);
        registroAcceso.validarEdicion(entity.getUsuario());

        entity.setEstado(!entity.isEstado());
        return toResponse(repository.save(entity));
    }

    private TipoDiagnostico validarTipo(TipoDiagnostico tipoDiagnostico) {
        if (tipoDiagnostico == null) {
            throw new IllegalArgumentException("El tipo de diagnóstico es obligatorio");
        }
        return tipoDiagnostico;
    }

    private String validarDescripcion(String descripcion) {
        String normalizada = StringNormalizer.normalizarTexto(descripcion);
        if (normalizada.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        return normalizada;
    }

    private diagnosticoEntity buscarEntidad(Long id) {
        diagnosticoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con el ID: " + id));
        registroAcceso.validarAccesoEpisodio(entity.getEpisodio());
        return entity;
    }

    private UsuarioEntity autor(Long usuarioId) {
        UsuarioEntity autenticado = registroAcceso.autorActual(usuarioId);
        return autenticado != null ? autenticado : buscarUsuario(usuarioId);
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

    private diagnosticoDTOs.Response toResponse(diagnosticoEntity entity) {
        return new diagnosticoDTOs.Response(
                entity.getId(),
                entity.getTipoDiagnostico(),
                entity.getDescripcion(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEstado());
    }
}
