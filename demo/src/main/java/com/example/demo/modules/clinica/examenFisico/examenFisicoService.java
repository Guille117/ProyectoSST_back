package com.example.demo.modules.clinica.examenFisico;

import com.example.demo.modules.clinica.parteCuerpo.parteCuerpoEntity;
import com.example.demo.modules.clinica.parteCuerpo.parteCuerpoRepository;
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
public class examenFisicoService {

    private final examenFisicoRepository repository;
    private final EpisodioRepository episodioRepository;
    private final parteCuerpoRepository parteCuerpoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAccesoService registroAcceso;

    @Transactional
    public examenFisicoDTOs.Response crear(examenFisicoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        examenFisicoEntity entity = examenFisicoEntity.builder()
                .descripcion(validarDescripcion(request.descripcion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .parteCuerpo(buscarParteCuerpo(request.parteCuerpoId()))
                .usuario(autor(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public examenFisicoDTOs.Response actualizar(Long id, examenFisicoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        examenFisicoEntity entity = buscarEntidad(id);
        registroAcceso.validarEdicion(entity.getUsuario());
        UsuarioEntity autorActualizado = registroAcceso.autorParaEdicion(entity.getUsuario(), request.usuarioId());
        entity.setUsuario(autorActualizado != null ? autorActualizado : buscarUsuario(request.usuarioId()));

        entity.setDescripcion(validarDescripcion(request.descripcion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setParteCuerpo(buscarParteCuerpo(request.parteCuerpoId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public examenFisicoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<examenFisicoDTOs.Response> obtenerTodos(boolean activo) {
        return repository.findByEstadoOrderByFechaDesc(activo).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<examenFisicoDTOs.Response> buscarPorEpisodio(Long episodioId, boolean activo) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }
        registroAcceso.validarAccesoEpisodio(episodioId);

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, activo).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<examenFisicoDTOs.Response> buscarPorParteCuerpo(Long parteCuerpoId, boolean activo) {
        if (parteCuerpoId == null) {
            throw new IllegalArgumentException("La parte del cuerpo es obligatoria para la búsqueda");
        }

        return repository.findByParteCuerpo_IdAndEstadoOrderByFechaDesc(parteCuerpoId, activo).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public examenFisicoDTOs.Response cambiarEstado(Long id) {
        examenFisicoEntity entity = buscarEntidad(id);
        registroAcceso.validarEdicion(entity.getUsuario());

        entity.setEstado(!entity.isEstado());
        return toResponse(repository.save(entity));
    }

    private String validarDescripcion(String descripcion) {
        String normalizada = StringNormalizer.normalizarTexto(descripcion);
        if (normalizada.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        return normalizada;
    }

    private examenFisicoEntity buscarEntidad(Long id) {
        examenFisicoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen físico no encontrado con el ID: " + id));
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

    private parteCuerpoEntity buscarParteCuerpo(Long parteCuerpoId) {
        if (parteCuerpoId == null) {
            throw new IllegalArgumentException("La parte del cuerpo es obligatoria");
        }

        return parteCuerpoRepository.findById(parteCuerpoId)
                .orElseThrow(() -> new IllegalArgumentException("Parte del cuerpo no encontrada con el ID: " + parteCuerpoId));
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

    private examenFisicoDTOs.Response toResponse(examenFisicoEntity entity) {
        return new examenFisicoDTOs.Response(
                entity.getId(),
                entity.getDescripcion(),
                entity.getEpisodio().getId(),
                entity.getParteCuerpo().getId(),
                entity.getParteCuerpo().getNombre(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEstado());
    }
}
