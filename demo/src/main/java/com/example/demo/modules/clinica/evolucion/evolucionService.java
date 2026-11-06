package com.example.demo.modules.clinica.evolucion;

import com.example.demo.modules.clinica.estadoClinico.estadoClinicoEntity;
import com.example.demo.modules.clinica.estadoClinico.estadoClinicoRepository;
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
public class evolucionService {

    private final evolucionRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final estadoClinicoRepository estadoClinicoRepository;
    private final RegistroAccesoService registroAcceso;

    @Transactional
    public evolucionDTOs.Response crear(evolucionDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String evolucionClinica = validarTexto(request.evolucionClinica(),
                "La evolución clínica es obligatoria");
        String planMedico = validarTexto(request.planMedico(),
                "El plan médico es obligatorio");
        EpisodioEntity episodio = buscarEpisodio(request.episodioId());
        estadoClinicoEntity estadoClinico = buscarEstadoClinico(request.estadoClinicoId());
        UsuarioEntity usuario = autor(request.usuarioId());

        episodio.setEstadoClinico(estadoClinico);
        episodioRepository.save(episodio);

        evolucionEntity entity = evolucionEntity.builder()
                .evolucionClinica(evolucionClinica)
                .planMedico(planMedico)
                .episodio(episodio)
                .usuario(usuario)
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
        registroAcceso.validarEdicion(entity.getUsuario());

        String evolucionClinica = validarTexto(request.evolucionClinica(),
                "La evolución clínica es obligatoria");
        String planMedico = validarTexto(request.planMedico(),
                "El plan médico es obligatorio");
        EpisodioEntity episodio = buscarEpisodio(request.episodioId());
        estadoClinicoEntity estadoClinico = buscarEstadoClinico(request.estadoClinicoId());

        episodio.setEstadoClinico(estadoClinico);
        episodioRepository.save(episodio);

        entity.setEvolucionClinica(evolucionClinica);
        entity.setPlanMedico(planMedico);
        entity.setEpisodio(episodio);
        UsuarioEntity autorActualizado = registroAcceso.autorParaEdicion(entity.getUsuario(), request.usuarioId());
        entity.setUsuario(autorActualizado != null ? autorActualizado : buscarUsuario(request.usuarioId()));

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
        registroAcceso.validarAccesoEpisodio(episodioId);

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(
                        episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        evolucionEntity entity = buscarEntidad(id);
        registroAcceso.validarEdicion(entity.getUsuario());
        entity.setEstado(EstadoEvolucion.ELIMINADA);
        repository.save(entity);
    }

    private evolucionEntity buscarEntidad(Long id) {
        evolucionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Evolución no encontrada con el ID: " + id));
        registroAcceso.validarAccesoEpisodio(entity.getEpisodio());
        return entity;
    }

    private UsuarioEntity autor(Long usuarioId) {
        UsuarioEntity autenticado = registroAcceso.autorActual(usuarioId);
        return autenticado != null ? autenticado : buscarUsuario(usuarioId);
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

        EpisodioEntity episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episodio no encontrado con el ID: " + episodioId));
        registroAcceso.validarAccesoEpisodio(episodio);
        return episodio;
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
        EpisodioEntity episodio = entity.getEpisodio();
        estadoClinicoEntity estadoClinico = episodio != null ? episodio.getEstadoClinico() : null;

        return new evolucionDTOs.Response(
                entity.getId(),
                entity.getEvolucionClinica(),
                entity.getPlanMedico(),
                episodio != null ? episodio.getId() : null,
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado(),
                estadoClinico != null ? estadoClinico.getId() : null,
                estadoClinico != null ? estadoClinico.getNombre() : null);
    }
}
