package com.example.demo.modules.catalogo;

import com.example.demo.utils.StringNormalizer;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
        if (entidad.getId() != null) {
            getRepository().findById(entidad.getId()).ifPresent(this::validarPuedeEditar);
        }
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
            validarPuedeDesactivar(entidadActual);
            entidadActual.setEstado(!entidadActual.isEstado());
            return Optional.of(getRepository().save(entidadActual));
        }
        return Optional.empty();
    }

    // aqui solo manda el nombre y el id no la entidad completa
    public Optional<T> actualizar(Long id, String nombre) {
        return actualizar(id, nombre, entidadActual -> {});
    }

    protected Optional<T> actualizar(Long id, String nombre, Consumer<T> actualizarCampos) {
        String nombreNormalizado = StringNormalizer.normalizarTexto(nombre);
        if (nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        Optional<T> entidadExistente = buscarPorId(id);
        if (entidadExistente.isPresent()) {
            validarPuedeEditar(entidadExistente.get());
            validarNoDuplicado(id, nombreNormalizado);
            T entidadActual = entidadExistente.get();
            entidadActual.setNombre(nombreNormalizado);
            actualizarCampos.accept(entidadActual);
            return Optional.of(getRepository().save(entidadActual));
        }
        return Optional.empty();
    }

    protected boolean estaRelacionado(T entidad) {
        return false;
    }

    protected void validarPropiedadDelSistema(T entidad) {
    }

    protected void validarPuedeEditar(T entidad) {
        validarPropiedadDelSistema(entidad);
        validarNoRelacionado(entidad, "editar");
    }

    protected void validarPuedeDesactivar(T entidad) {
        validarPropiedadDelSistema(entidad);
        if (entidad.isEstado()) {
            validarNoRelacionado(entidad, "desactivar");
        }
    }

    private void validarNoRelacionado(T entidad, String accion) {
        if (estaRelacionado(entidad)) {
            throw new IllegalArgumentException(
                    "No se puede " + accion + " el registro porque está relacionado con otro registro.");
        }
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