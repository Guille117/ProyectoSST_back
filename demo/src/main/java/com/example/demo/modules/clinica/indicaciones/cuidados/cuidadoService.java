package com.example.demo.modules.clinica.indicaciones.cuidados;

import com.example.demo.modules.clinica.indicaciones.DuracionValidator;
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
public class cuidadoService {

    private final cuidadoRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public cuidadoDTOs.Response crear(cuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        cuidadoEntity entity = cuidadoEntity.builder()
                .nombreCuidado(validarNombre(request.nombreCuidado()))
                .frecuencia(validarFrecuencia(request.frecuencia()))
                .duracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()))
                .tipoDuracion(request.tipoDuracion())
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public cuidadoDTOs.Response actualizar(Long id, cuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        cuidadoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuidado no encontrado con el ID: " + id));

        entity.setNombreCuidado(validarNombre(request.nombreCuidado()));
        entity.setFrecuencia(validarFrecuencia(request.frecuencia()));
        entity.setDuracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()));
        entity.setTipoDuracion(request.tipoDuracion());
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public cuidadoDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Cuidado no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<cuidadoDTOs.Response> obtenerTodos(EstadoCuidado estado) {
        return repository.findByEstado(validarEstado(estado)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<cuidadoDTOs.Response> buscarPorEpisodio(Long episodioId, EstadoCuidado estado) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstado(episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public cuidadoDTOs.Response cambiarEstado(Long id, EstadoCuidado estado) {
        cuidadoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuidado no encontrado con el ID: " + id));

        entity.setEstado(validarEstado(estado));
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void eliminar(Long id) {
        cuidadoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuidado no encontrado con el ID: " + id));

        entity.setEstado(EstadoCuidado.ELIMINADO);
        repository.save(entity);
    }

    private EstadoCuidado validarEstado(EstadoCuidado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
    }

    private String validarNombre(String nombreCuidado) {
        String normalizado = StringNormalizer.normalizarTexto(nombreCuidado);
        if (normalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre del cuidado es obligatorio");
        }
        return normalizado;
    }

    private Integer validarFrecuencia(Integer frecuencia) {
        if (frecuencia == null) {
            throw new IllegalArgumentException("La frecuencia es obligatoria");
        }
        if (frecuencia <= 0) {
            throw new IllegalArgumentException("La frecuencia debe ser mayor a 0");
        }
        return frecuencia;
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

    private cuidadoDTOs.Response toResponse(cuidadoEntity entity) {
        return new cuidadoDTOs.Response(
                entity.getId(),
                entity.getNombreCuidado(),
                entity.getFrecuencia(),
                entity.getDuracion(),
                entity.getTipoDuracion(),
                entity.getObservacion(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado());
    }
}
