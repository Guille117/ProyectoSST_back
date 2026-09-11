package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PuestoService {

    private final PuestoRepository repository;
    private final PuestoMapper mapper;

    @Transactional
    public PuestoDTOs.Response crear(PuestoDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        validarNoDuplicado(null, nombre);

        PuestoEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setCodigo(generarCodigoPuesto());

        PuestoEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public PuestoDTOs.Response actualizar(Long id, PuestoDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        PuestoEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado"));

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        validarNoDuplicado(id, nombre);

        existente.setNombre(nombre);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());

        PuestoEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public PuestoDTOs.Response obtenerPorId(Long id) {
        PuestoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<PuestoDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream()
                    .map(mapper::toDTO)
                    .toList();
        }
        return repository.findByEstado(activos).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PuestoDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }
        String nombreNormalizado = nombre.trim();
        if (activos == null) {
            return repository.findByNombreContainingIgnoreCase(nombreNormalizado).stream()
                    .map(mapper::toDTO)
                    .toList();
        }
        return repository.findByNombreContainingIgnoreCaseAndEstado(nombreNormalizado, activos).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        PuestoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<PuestoEntity> duplicado = repository.findByNombreIgnoreCase(nombre);
        if (duplicado.isEmpty() || duplicado.get().getId().equals(idActual)) {
            return;
        }
        if (duplicado.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un puesto activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException(
                "Ya existe un puesto con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo."
        );
    }

    private String generarCodigoPuesto() {
        long total = repository.count();
        return String.format("PUE-%02d", total + 1);
    }
}
