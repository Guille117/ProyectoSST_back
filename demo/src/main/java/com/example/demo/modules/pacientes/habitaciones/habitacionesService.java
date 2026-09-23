package com.example.demo.modules.pacientes.habitaciones;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.utils.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class habitacionesService extends serviceBase<habitacionesEntity> {

    private final habitacionesRepository repository;

    public habitacionesService(habitacionesRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<habitacionesEntity> getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public habitacionesEntity guardar(habitacionesEntity entidad) {
        entidad.setDescripcion(StringNormalizer.normalizarTexto(entidad.getDescripcion()));
        return super.guardar(entidad);
    }

    @Transactional
    public habitacionesEntity actualizarConDescripcion(Long id, habitacionesEntity detalles) {
        habitacionesEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada con el ID: " + id));
        existente.setNombre(StringNormalizer.normalizarTexto(detalles.getNombre()));
        existente.setDescripcion(StringNormalizer.normalizarTexto(detalles.getDescripcion()));
        return guardar(existente);
    }

    @Transactional(readOnly = true)
    public List<habitacionesEntity> buscarPorNombre(String nombre) {
        return repository.findAll().stream()
                .filter(item -> item.getNombre().toLowerCase().contains(nombre.trim().toLowerCase()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }
}