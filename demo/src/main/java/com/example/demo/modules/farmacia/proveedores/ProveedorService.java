package com.example.demo.modules.farmacia.proveedores;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository repository;
    private final ProveedorMapper mapper;

    @Transactional
    public ProveedorDTOs.Response crear(ProveedorDTOs.Request req) {
        if (req.nit() != null && repository.existsByNit(req.nit())) {
            throw new RuntimeException("Ya existe un proveedor registrado con el NIT: " + req.nit());
        }

        ProveedorEntity entity = mapper.toEntity(req);
        entity.setCodigo(generarCodigoProveedor());

        ProveedorEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public ProveedorDTOs.Response obtenerPorId(Long id) {
        ProveedorEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTOs.Response> obtenerTodos() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    private String generarCodigoProveedor() {
        long total = repository.count();
        return String.format("PROV-%02d", total + 1);
    }
}
