package com.example.demo.modules.catalogo;

import com.example.demo.utils.StringNormalizer;
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
        String nombre = StringNormalizer.normalizarTexto(entidad.getNombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        entidad.setNombre(nombre);
        validarNoDuplicado(entidad.getId(), nombre);
        return getRepository().save(entidad);
    }

    public Optional<T> cambiarEstado(Long id) {
        Optional<T> entidadExistente = buscarPorId(id);
        if (entidadExistente.isPresent()) {
            T entidadActual = entidadExistente.get();
            entidadActual.setEstado(!entidadActual.isEstado());
            return Optional.of(getRepository().save(entidadActual));
        }
        return Optional.empty();
    }

    // aqui solo manda el nombre y el id no la entidad completa
    public Optional<T> actualizar(Long id, String nombre) {
        String nombreNormalizado = StringNormalizer.normalizarTexto(nombre);
        if (nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        Optional<T> entidadExistente = buscarPorId(id);
        if (entidadExistente.isPresent()) {
            validarNoDuplicado(id, nombreNormalizado);
            T entidadActual = entidadExistente.get();
            entidadActual.setNombre(nombreNormalizado);
            return Optional.of(getRepository().save(entidadActual));
        }
        return Optional.empty();
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<T> duplicado = getRepository().findByNombreIgnoreCase(nombre);
        if (duplicado.isPresent() && !duplicado.get().getId().equals(idActual)) {
            T existente = duplicado.get();
            if (existente.isEstado()) {
                throw new IllegalArgumentException("Ya existe un registro activo con el nombre: " + nombre);
            } else {
                throw new IllegalArgumentException("Ya existe un registro inactivo con el nombre: " + nombre + ". Actívalo primero o actualiza ese registro inactivo.");
            }
        }
    }
}