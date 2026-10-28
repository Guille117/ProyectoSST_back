package com.example.demo.modules.enfermeria.notas;

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
public class notaEnfermeriaService {

    private final notaEnfermeriaRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public notaEnfermeriaDTOs.Response crear(notaEnfermeriaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        notaEnfermeriaEntity entity = notaEnfermeriaEntity.builder()
                .texto(validarTexto(request.texto()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public notaEnfermeriaDTOs.Response actualizar(Long id, notaEnfermeriaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        notaEnfermeriaEntity entity = buscar(id);
        entity.setTexto(validarTexto(request.texto()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public notaEnfermeriaDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<notaEnfermeriaDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(episodioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        notaEnfermeriaEntity entity = buscar(id);
        entity.setEliminado(true);
        repository.save(entity);
    }

    private notaEnfermeriaEntity buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota de enfermería no encontrada con el ID: " + id));
    }

    private String validarTexto(String texto) {
        String normalizado = StringNormalizer.normalizarTexto(texto);
        if (normalizado.isBlank()) {
            throw new IllegalArgumentException("El texto de la nota es obligatorio");
        }
        return normalizado;
    }

    private EpisodioEntity buscarEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        return episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado con el ID: " + episodioId));
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

    private notaEnfermeriaDTOs.Response toResponse(notaEnfermeriaEntity entity) {
        return new notaEnfermeriaDTOs.Response(
                entity.getId(),
                entity.getTexto(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEliminado());
    }
}
