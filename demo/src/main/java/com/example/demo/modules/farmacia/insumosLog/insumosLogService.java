package com.example.demo.modules.farmacia.insumosLog;

import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class insumosLogService {
    private final insumosLogRepository repository;
    private final marcaRepository marcaRepository;

    @Transactional
    public insumosLogDTOs.Response crear(insumosLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("La solicitud es obligatoria");
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        validarNombreUnico(null, nombre);
        insumosLogEntity entity = insumosLogEntity.builder().nombre(nombre).build();
        asignarMarca(entity, request.marcaId());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public insumosLogDTOs.Response actualizar(Long id, insumosLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("Sin datos para actualizar");
        insumosLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        validarNombreUnico(id, nombre);
        entity.setNombre(nombre);
        asignarMarca(entity, request.marcaId());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public insumosLogDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<insumosLogDTOs.Response> obtenerTodos() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<insumosLogDTOs.Response> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return List.of();
        return repository.findByNombreContainingIgnoreCase(nombre.trim()).stream().map(this::toResponse).toList();
    }

    private void asignarMarca(insumosLogEntity entity, Long marcaId) {
        entity.setMarca(marcaRepository.findById(marcaId)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con el ID: " + marcaId)));
    }

    private void validarNombreUnico(Long idActual, String nombre) {
        repository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un insumo con el nombre: " + nombre);
            }
        });
    }

    private insumosLogDTOs.Response toResponse(insumosLogEntity entity) {
        return new insumosLogDTOs.Response(entity.getId(), entity.getNombre(), entity.getMarca().getId(), entity.getMarca().getNombre());
    }
}