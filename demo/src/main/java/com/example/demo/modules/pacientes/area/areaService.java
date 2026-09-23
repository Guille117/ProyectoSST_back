package com.example.demo.modules.pacientes.area;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.utils.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class areaService extends serviceBase<areaEntity> {

    private final areaRepository repository;

    public areaService(areaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<areaEntity> getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public areaEntity guardar(areaEntity entidad) {
        entidad.setDescripcion(StringNormalizer.normalizarTexto(entidad.getDescripcion()));
        return super.guardar(entidad);
    }

    @Transactional
    public areaEntity actualizarConDescripcion(Long id, areaEntity detalles) {
        areaEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con el ID: " + id));
        existente.setNombre(StringNormalizer.normalizarTexto(detalles.getNombre()));
        existente.setDescripcion(StringNormalizer.normalizarTexto(detalles.getDescripcion()));
        return guardar(existente);
    }

    @Transactional(readOnly = true)
    public List<areaEntity> buscarPorNombre(String nombre) {
        return repository.findAll().stream()
                .filter(item -> item.getNombre().toLowerCase().contains(nombre.trim().toLowerCase()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }
}