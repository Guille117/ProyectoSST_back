package com.example.demo.modules.farmacia.proveedores;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository repository;
    private final ProveedorMapper mapper;

    @Transactional
    public ProveedorDTOs.Response crear(ProveedorDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        String nit = StringNormalizer.normalizarNullable(req.nit());
        if (nit != null) {
            nit = nit.toUpperCase(Locale.ROOT);
        }
        String telefono = StringNormalizer.normalizarNullable(req.telefono());
        String email = StringNormalizer.normalizarNullable(req.email());

        validarNoDuplicado(null, nombre, nit, telefono, email);

        ProveedorEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setNit(nit);
        entity.setTelefono(telefono);
        entity.setEmail(email);
        entity.setCodigo(generarCodigoProveedor());

        ProveedorEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public ProveedorDTOs.Response actualizar(Long id, ProveedorDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        ProveedorEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        String nit = StringNormalizer.normalizarNullable(req.nit());
        if (nit != null) {
            nit = nit.toUpperCase(Locale.ROOT);
        }
        String telefono = StringNormalizer.normalizarNullable(req.telefono());
        String email = StringNormalizer.normalizarNullable(req.email());

        validarNoDuplicado(id, nombre, nit, telefono, email);

        existente.setNombre(nombre);
        existente.setNit(nit);
        existente.setTelefono(telefono);
        existente.setEmail(email);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());

        ProveedorEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public ProveedorDTOs.Response obtenerPorId(Long id) {
        ProveedorEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTOs.Response> obtenerTodos(Boolean activos) {
        // if (activos == null) {
        //     return repository.findAll().stream()
        //             .map(mapper::toDTO)
        //             .toList();
        // }

        System.out.println("activos: " + activos);

        return repository.findByEstado(activos).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
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
    public void cambiarEstado(Long id){
        ProveedorEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con el ID: " + id));

        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre, String nit, String telefono, String email) {
        if (nombre != null && !nombre.isBlank()) {
            validacionDeUnicidadPorNombre(idActual, nombre);
        }

        if (nit != null) {
            if (!nit.equalsIgnoreCase("CF") && !nit.matches("^[0-9]{7}-[0-9Kk]$")) {
                throw new IllegalArgumentException("NIT inválido. Formatos aceptados: 1234567-8, 1234567-K o CF");
            }
            validacionDeUnicidadPorCampo(idActual, nit, repository::findByNitIgnoreCase, "NIT");
        }

        if (telefono != null) {
            if (!telefono.matches("^[0-9]{8}$")) {
                throw new IllegalArgumentException("El teléfono debe tener exactamente 8 dígitos numéricos");
            }
            validacionDeUnicidadPorCampo(idActual, telefono, repository::findByTelefono, "teléfono");
        }

        if (email != null) {
            validacionDeUnicidadPorCampo(idActual, email, repository::findByEmailIgnoreCase, "email");
        }
    }

    private void validacionDeUnicidadPorNombre(Long idActual, String nombre) {
        Optional<ProveedorEntity> duplicado = repository.findByNombreIgnoreCase(nombre);
        if (duplicado.isEmpty() || duplicado.get().getId().equals(idActual)) {

            return;
        }

        if (duplicado.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un proveedor activo con el nombre: " + nombre);
        }

        throw new IllegalArgumentException(
                "Ya existe un proveedor con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo."
        );
    }

    private void validacionDeUnicidadPorCampo(Long idActual,
                                            String valor,
                                            java.util.function.Function<String, Optional<ProveedorEntity>> buscador,
                                            String campo) {
        Optional<ProveedorEntity> duplicado = buscador.apply(valor);
        if (duplicado.isEmpty() || duplicado.get().getId().equals(idActual)) {
            return;
        }

        if (duplicado.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un proveedor activo con el " + campo + ": " + valor);
        }

        throw new IllegalArgumentException(
                "Ya existe un proveedor con el " + campo + ": " + valor + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo."
        );
    }

    private String generarCodigoProveedor() {
        long total = repository.count();
        return String.format("PROV-%02d", total + 1);
    }
}
