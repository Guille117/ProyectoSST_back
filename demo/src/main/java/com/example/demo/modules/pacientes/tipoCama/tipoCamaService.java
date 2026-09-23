package com.example.demo.modules.pacientes.tipoCama;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.utils.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class tipoCamaService extends serviceBase<tipoCamaEntity> {

    private final tipoCamaRepository repository;

    public tipoCamaService(tipoCamaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<tipoCamaEntity> getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public tipoCamaEntity guardar(tipoCamaEntity entidad) {
        entidad.setDescripcion(StringNormalizer.normalizarTexto(entidad.getDescripcion()));
        return super.guardar(entidad);
    }

    @Transactional
    public tipoCamaEntity actualizarConDescripcion(Long id, tipoCamaEntity detalles) {
        tipoCamaEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de cama no encontrado con el ID: " + id));
        existente.setNombre(StringNormalizer.normalizarTexto(detalles.getNombre()));
        existente.setDescripcion(StringNormalizer.normalizarTexto(detalles.getDescripcion()));
        return guardar(existente);
    }

    @Transactional(readOnly = true)
    public List<tipoCamaEntity> buscarPorNombre(String nombre) {
        return repository.findAll().stream()
                .filter(item -> item.getNombre().toLowerCase().contains(nombre.trim().toLowerCase()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }
}