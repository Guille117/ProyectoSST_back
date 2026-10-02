package com.example.demo.modules.pacientes.institucion;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.utils.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class institucionService extends serviceBase<institucionEntity> {

    private final institucionRepository repository;

    public institucionService(institucionRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<institucionEntity> getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public institucionEntity guardar(institucionEntity entidad) {
        entidad.setDireccion(StringNormalizer.normalizarNullable(entidad.getDireccion()));
        entidad.setTelefono(StringNormalizer.normalizarNullable(entidad.getTelefono()));
        return super.guardar(entidad);
    }

    @Transactional
    public institucionEntity actualizar(Long id, institucionEntity detalles) {
        institucionEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institución no encontrada con el ID: " + id));
        existente.setNombre(StringNormalizer.normalizarTexto(detalles.getNombre()));
        existente.setDireccion(StringNormalizer.normalizarNullable(detalles.getDireccion()));
        existente.setTelefono(StringNormalizer.normalizarNullable(detalles.getTelefono()));
        return guardar(existente);
    }

    @Transactional(readOnly = true)
    public List<institucionEntity> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }
        String criterio = nombre.trim().toLowerCase(java.util.Locale.ROOT);
        return repository.findAll().stream()
                .filter(institucion -> institucion.getNombre() != null
                        && institucion.getNombre().toLowerCase(java.util.Locale.ROOT).contains(criterio))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return repository.countByEstado(true);
    }
}