package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import com.example.demo.modules.clinica.indicaciones.cuidados.EstadoCuidado;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoEntity;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoRepository;
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
public class ejecucionCuidadoService {

    private final ejecucionCuidadoRepository repository;
    private final cuidadoRepository cuidadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ejecucionCuidadoDTOs.Response crear(ejecucionCuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        cuidadoEntity cuidado = buscarCuidado(request.cuidadoId());
        if (cuidado.getEstado() == EstadoCuidado.ELIMINADO) {
            throw new IllegalArgumentException("El cuidado está eliminado");
        }
        if (request.fechaHoraProgramada() == null) {
            throw new IllegalArgumentException("La fecha y hora programada son obligatorias");
        }
        if (repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(
                cuidado.getId(), request.fechaHoraProgramada())) {
            throw new IllegalArgumentException("Ya existe un registro para esa ejecución");
        }

        ejecucionCuidadoEntity entity = ejecucionCuidadoEntity.builder()
                .cuidado(cuidado)
                .fechaHoraProgramada(request.fechaHoraProgramada())
                .estado(validarEstado(request.estado()))
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fechaRegistro(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public ejecucionCuidadoDTOs.Response actualizar(Long id, ejecucionCuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        ejecucionCuidadoEntity entity = buscar(id);
        entity.setEstado(validarEstado(request.estado()));
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ejecucionCuidadoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscar(Long cuidadoId, Long episodioId) {
        if (cuidadoId != null && episodioId != null) {
            throw new IllegalArgumentException("Indique el cuidado o el episodio, no ambos");
        }
        if (cuidadoId != null) {
            return buscarPorCuidado(cuidadoId);
        }
        if (episodioId != null) {
            return buscarPorEpisodio(episodioId);
        }
        throw new IllegalArgumentException("Debe indicar el cuidado o el episodio");
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscarPorCuidado(Long cuidadoId) {
        return repository.findByCuidado_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(cuidadoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscarPorEpisodio(Long episodioId) {
        return repository.findByCuidado_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        ejecucionCuidadoEntity entity = buscar(id);
        entity.setEliminado(true);
        repository.save(entity);
    }

    private ejecucionCuidadoEntity buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejecución de cuidado no encontrada con el ID: " + id));
    }

    private cuidadoEntity buscarCuidado(Long cuidadoId) {
        if (cuidadoId == null) {
            throw new IllegalArgumentException("El cuidado es obligatorio");
        }
        return cuidadoRepository.findById(cuidadoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuidado no encontrado con el ID: " + cuidadoId));
    }

    private EstadoEjecucionCuidado validarEstado(EstadoEjecucionCuidado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
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

    private ejecucionCuidadoDTOs.Response toResponse(ejecucionCuidadoEntity entity) {
        return new ejecucionCuidadoDTOs.Response(
                entity.getId(),
                entity.getCuidado().getId(),
                entity.getFechaHoraProgramada(),
                entity.getEstado(),
                entity.getObservacion(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFechaRegistro());
    }
}
