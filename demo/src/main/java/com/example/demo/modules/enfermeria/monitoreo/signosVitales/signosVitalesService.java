package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

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
public class signosVitalesService {

    private final signosVitalesRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public signosVitalesDTOs.Response crear(signosVitalesDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        signosVitalesEntity entity = signosVitalesEntity.builder()
                .sistolica(request.sistolica())
                .diastolica(request.diastolica())
                .frecuenciaCardiaca(request.frecuenciaCardiaca())
                .frecuenciaRespiratoria(request.frecuenciaRespiratoria())
                .temperatura(request.temperatura())
                .saturacion(request.saturacion())
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public signosVitalesDTOs.Response actualizar(Long id, signosVitalesDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        signosVitalesEntity entity = buscar(id);
        entity.setSistolica(request.sistolica());
        entity.setDiastolica(request.diastolica());
        entity.setFrecuenciaCardiaca(request.frecuenciaCardiaca());
        entity.setFrecuenciaRespiratoria(request.frecuenciaRespiratoria());
        entity.setTemperatura(request.temperatura());
        entity.setSaturacion(request.saturacion());
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public signosVitalesDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<signosVitalesDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(episodioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        signosVitalesEntity entity = buscar(id);
        entity.setEliminado(true);
        repository.save(entity);
    }

    private signosVitalesEntity buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de signos vitales no encontrado con el ID: " + id));
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

    private signosVitalesDTOs.Response toResponse(signosVitalesEntity entity) {
        return new signosVitalesDTOs.Response(
                entity.getId(),
                entity.getSistolica(),
                entity.getDiastolica(),
                entity.getFrecuenciaCardiaca(),
                entity.getFrecuenciaRespiratoria(),
                entity.getTemperatura(),
                entity.getSaturacion(),
                entity.getObservacion(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.isEliminado());
    }
}
