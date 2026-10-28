package com.example.demo.modules.clinica.evolucion;

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
public class evolucionService {

    private final evolucionRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public evolucionDTOs.Response crear(evolucionDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        evolucionEntity entity = evolucionEntity.builder()
                .evolucionClinica(validarTexto(request.evolucionClinica(),
                        "La evolución clínica es obligatoria"))
                .planMedico(validarTexto(request.planMedico(),
                        "El plan médico es obligatorio"))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public evolucionDTOs.Response actualizar(Long id, evolucionDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        evolucionEntity entity = buscarEntidad(id);

        entity.setEvolucionClinica(validarTexto(request.evolucionClinica(),
                "La evolución clínica es obligatoria"));
        entity.setPlanMedico(validarTexto(request.planMedico(),
                "El plan médico es obligatorio"));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public evolucionDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<evolucionDTOs.Response> buscarPorEpisodio(Long episodioId, EstadoEvolucion estado) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(
                        episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        evolucionEntity entity = buscarEntidad(id);
        entity.setEstado(EstadoEvolucion.ELIMINADA);
        repository.save(entity);
    }

    private evolucionEntity buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Evolución no encontrada con el ID: " + id));
    }

    private EstadoEvolucion validarEstado(EstadoEvolucion estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
    }

    private String validarTexto(String valor, String mensaje) {
        String normalizado = StringNormalizer.normalizarTexto(valor);
        if (normalizado.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return normalizado;
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

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        String nombreCompleto = usuario.getPersona() != null
                ? usuario.getPersona().getNombreCompleto()
                : "";
        return nombreCompleto.isBlank() ? usuario.getUsername() : nombreCompleto;
    }

    private evolucionDTOs.Response toResponse(evolucionEntity entity) {
        return new evolucionDTOs.Response(
                entity.getId(),
                entity.getEvolucionClinica(),
                entity.getPlanMedico(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado());
    }
}
