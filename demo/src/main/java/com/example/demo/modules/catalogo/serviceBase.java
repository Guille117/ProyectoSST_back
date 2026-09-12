package com.example.demo.modules.catalogo;

import java.util.List;
import java.util.Optional;

public abstract class serviceBase <T extends entityBase>{
    protected abstract repositoryBase<T> getRepository();

    public List<T> listarTodos() {
        return getRepository().findAll();
    }

    public List<T> listarPorEstado(boolean estado) {
        return getRepository().findByEstado(estado);
    }

    public Optional<T> buscarPorId(Long id) {
        return getRepository().findById(id);
    }

    public T guardar(T entidad) {
        return getRepository().save(entidad);
    }

    public Optional<T> cambiarEstado(Long id) {
        Optional<T> entidadExistente = buscarPorId(id);
        if (entidadExistente.isPresent()) {
            T entidadActual = entidadExistente.get();
            entidadActual.setEstado(!entidadActual.isEstado());
            return Optional.of(guardar(entidadActual));
        }
        return Optional.empty();
    }

    // aqui solo manda el nombre y el id no la entidad completa
    public Optional<T> actualizar(Long id, String nombre) {
        Optional<T> entidadExistente = buscarPorId(id);
        if (entidadExistente.isPresent()) {
            T entidadActual = entidadExistente.get();
            entidadActual.setNombre(nombre);
            return Optional.of(guardar(entidadActual));
        }
        return Optional.empty();
    }
}