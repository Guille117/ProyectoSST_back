package com.example.demo.modules.clinica.historiaClinica;

import com.example.demo.modules.clinica.estadoClinico.estadoClinicoEntity;
import com.example.demo.modules.clinica.estadoClinico.estadoClinicoRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class historiaClinicaService {

    private final historiaClinicaRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final estadoClinicoRepository estadoClinicoRepository;

    @Transactional
    public historiaClinicaDTOs.Response crear(historiaClinicaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String descripcion = validarDescripcion(request.descripcion());
        EpisodioEntity episodio = buscarEpisodio(request.episodioId());
        estadoClinicoEntity estadoClinico = buscarEstadoClinico(request.estadoClinicoId());
        UsuarioEntity usuario = buscarUsuario(request.usuarioId());

        episodio.setEstadoClinico(estadoClinico);
        episodioRepository.save(episodio);

        historiaClinicaEntity entity = historiaClinicaEntity.builder()
                .descripcion(descripcion)
                .episodio(episodio)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public historiaClinicaDTOs.Response actualizar(Long id, historiaClinicaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        historiaClinicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada con el ID: " + id));

        String descripcion = validarDescripcion(request.descripcion());
        EpisodioEntity episodio = buscarEpisodio(request.episodioId());
        estadoClinicoEntity estadoClinico = buscarEstadoClinico(request.estadoClinicoId());
        UsuarioEntity usuario = buscarUsuario(request.usuarioId());

        episodio.setEstadoClinico(estadoClinico);
        episodioRepository.save(episodio);

        entity.setDescripcion(descripcion);
        entity.setEpisodio(episodio);
        entity.setUsuario(usuario);

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public historiaClinicaDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<historiaClinicaDTOs.Response> obtenerTodos(boolean activo) {
        return repository.findByEstadoOrderByFechaDesc(activo).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<historiaClinicaDTOs.Response> buscarPorEpisodio(Long episodioId, boolean activo) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, activo).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public historiaClinicaDTOs.Response cambiarEstado(Long id) {
        historiaClinicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada con el ID: " + id));

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

    private EpisodioEntity buscarEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        return episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado con el ID: " + episodioId));
    }

    private estadoClinicoEntity buscarEstadoClinico(Long estadoClinicoId) {
        if (estadoClinicoId == null) {
            throw new IllegalArgumentException("El estado clínico es obligatorio");
        }

        return estadoClinicoRepository.findById(estadoClinicoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado clínico no encontrado con el ID: " + estadoClinicoId));
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

    private historiaClinicaDTOs.Response toResponse(historiaClinicaEntity entity) {
        EpisodioEntity episodio = entity.getEpisodio();
        estadoClinicoEntity estadoClinico = episodio != null ? episodio.getEstadoClinico() : null;

        return new historiaClinicaDTOs.Response(
                entity.getId(),
                entity.getDescripcion(),
                episodio != null ? episodio.getId() : null,
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEstado(),
                estadoClinico != null ? estadoClinico.getId() : null,
                estadoClinico != null ? estadoClinico.getNombre() : null);
    }
}
