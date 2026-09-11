package com.example.demo.modules.enfermeria.nose2;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnfermeriaNose2Service {

    private final EnfermeriaNose2Repository repository;
    private final EnfermeriaNose2Mapper mapper;

    @Transactional
    public EnfermeriaNose2DTOs.Response crear(EnfermeriaNose2DTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(null, nombre);
        EnfermeriaNose2Entity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setCodigo(generarCodigo());
        EnfermeriaNose2Entity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public EnfermeriaNose2DTOs.Response actualizar(Long id, EnfermeriaNose2DTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        EnfermeriaNose2Entity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EnfermeriaNose2 no encontrado"));
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(id, nombre);
        existente.setNombre(nombre);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());
        EnfermeriaNose2Entity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public EnfermeriaNose2DTOs.Response obtenerPorId(Long id) {
        EnfermeriaNose2Entity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EnfermeriaNose2 no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<EnfermeriaNose2DTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream().map(mapper::toDTO).toList();
        }
        return repository.findByEstado(activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EnfermeriaNose2DTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }
        String norm = nombre.trim();
        if (activos == null) {
            return repository.findByNombreContainingIgnoreCase(norm).stream().map(mapper::toDTO).toList();
        }
        return repository.findByNombreContainingIgnoreCaseAndEstado(norm, activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        EnfermeriaNose2Entity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EnfermeriaNose2 no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<EnfermeriaNose2Entity> dup = repository.findByNombreIgnoreCase(nombre);
        if (dup.isEmpty() || dup.get().getId().equals(idActual)) {
            return;
        }
        if (dup.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un EnfermeriaNose2 activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException("Ya existe un EnfermeriaNose2 con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo.");
    }

    private String generarCodigo() {
        long total = repository.count();
        return String.format("ENF2-%02d", total + 1);
    }
}
