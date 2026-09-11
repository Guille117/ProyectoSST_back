package com.example.demo.modules.paciente.camas;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CamaService {

    private final CamaRepository repository;
    private final CamaMapper mapper;

    @Transactional
    public CamaDTOs.Response crear(CamaDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(null, nombre);
        CamaEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setCodigo(generarCodigo());
        CamaEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public CamaDTOs.Response actualizar(Long id, CamaDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        CamaEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrado"));
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(id, nombre);
        existente.setNombre(nombre);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());
        CamaEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public CamaDTOs.Response obtenerPorId(Long id) {
        CamaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<CamaDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream().map(mapper::toDTO).toList();
        }
        return repository.findByEstado(activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CamaDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
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
        CamaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<CamaEntity> dup = repository.findByNombreIgnoreCase(nombre);
        if (dup.isEmpty() || dup.get().getId().equals(idActual)) {
            return;
        }
        if (dup.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un Cama activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException("Ya existe un Cama con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo.");
    }

    private String generarCodigo() {
        long total = repository.count();
        return String.format("CAMA-%02d", total + 1);
    }
}
