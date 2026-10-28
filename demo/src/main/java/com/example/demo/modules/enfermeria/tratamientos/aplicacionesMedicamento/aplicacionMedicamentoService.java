package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import com.example.demo.modules.clinica.indicaciones.medicamentos.EstadoIndicacionMedicamento;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoEntity;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoRepository;
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
public class aplicacionMedicamentoService {

    private final aplicacionMedicamentoRepository repository;
    private final indicacionMedicamentoRepository indicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public aplicacionMedicamentoDTOs.Response crear(aplicacionMedicamentoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        indicacionMedicamentoEntity indicacion = buscarIndicacion(request.indicacionMedicamentoId());
        if (indicacion.getEstado() == EstadoIndicacionMedicamento.ELIMINADO) {
            throw new IllegalArgumentException("La indicación está eliminada");
        }
        if (request.fechaHoraProgramada() == null) {
            throw new IllegalArgumentException("La fecha y hora programada son obligatorias");
        }
        if (repository.existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalse(
                indicacion.getId(), request.fechaHoraProgramada())) {
            throw new IllegalArgumentException("Ya existe un registro para esa aplicación");
        }

        aplicacionMedicamentoEntity entity = aplicacionMedicamentoEntity.builder()
                .indicacion(indicacion)
                .fechaHoraProgramada(request.fechaHoraProgramada())
                .estado(validarEstado(request.estado()))
                .cantidad(request.cantidad())
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fechaRegistro(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public aplicacionMedicamentoDTOs.Response actualizar(Long id, aplicacionMedicamentoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        aplicacionMedicamentoEntity entity = buscar(id);
        entity.setEstado(validarEstado(request.estado()));
        entity.setCantidad(request.cantidad());
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public aplicacionMedicamentoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<aplicacionMedicamentoDTOs.Response> buscar(Long indicacionId, Long episodioId) {
        if (indicacionId != null && episodioId != null) {
            throw new IllegalArgumentException("Indique la indicación o el episodio, no ambos");
        }
        if (indicacionId != null) {
            return buscarPorIndicacion(indicacionId);
        }
        if (episodioId != null) {
            return buscarPorEpisodio(episodioId);
        }
        throw new IllegalArgumentException("Debe indicar la indicación o el episodio");
    }

    @Transactional(readOnly = true)
    public List<aplicacionMedicamentoDTOs.Response> buscarPorIndicacion(Long indicacionId) {
        return repository.findByIndicacion_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(indicacionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<aplicacionMedicamentoDTOs.Response> buscarPorEpisodio(Long episodioId) {
        return repository.findByIndicacion_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        aplicacionMedicamentoEntity entity = buscar(id);
        entity.setEliminado(true);
        repository.save(entity);
    }

    private aplicacionMedicamentoEntity buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación de medicamento no encontrada con el ID: " + id));
    }

    private indicacionMedicamentoEntity buscarIndicacion(Long indicacionId) {
        if (indicacionId == null) {
            throw new IllegalArgumentException("La indicación es obligatoria");
        }
        return indicacionRepository.findById(indicacionId)
                .orElseThrow(() -> new IllegalArgumentException("Indicación no encontrada con el ID: " + indicacionId));
    }

    private EstadoAplicacionMedicamento validarEstado(EstadoAplicacionMedicamento estado) {
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

    private aplicacionMedicamentoDTOs.Response toResponse(aplicacionMedicamentoEntity entity) {
        return new aplicacionMedicamentoDTOs.Response(
                entity.getId(),
                entity.getIndicacion().getId(),
                entity.getFechaHoraProgramada(),
                entity.getEstado(),
                entity.getCantidad(),
                entity.getObservacion(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFechaRegistro());
    }
}
