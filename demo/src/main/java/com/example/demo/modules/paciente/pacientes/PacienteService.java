package com.example.demo.modules.paciente.pacientes;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository repository;
    private final PacienteMapper mapper;

    @Transactional
    public PacienteDTOs.Response crear(PacienteDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(null, nombre);
        PacienteEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setCodigo(generarCodigo());
        PacienteEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public PacienteDTOs.Response actualizar(Long id, PacienteDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        PacienteEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        validarNoDuplicado(id, nombre);
        existente.setNombre(nombre);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());
        PacienteEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public PacienteDTOs.Response obtenerPorId(Long id) {
        PacienteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<PacienteDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream().map(mapper::toDTO).toList();
        }
        return repository.findByEstado(activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PacienteDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
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
        PacienteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<PacienteEntity> dup = repository.findByNombreIgnoreCase(nombre);
        if (dup.isEmpty() || dup.get().getId().equals(idActual)) {
            return;
        }
        if (dup.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un Paciente activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException("Ya existe un Paciente con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo.");
    }

    private String generarCodigo() {
        long total = repository.count();
        return String.format("PAC-%02d", total + 1);
    }
}
