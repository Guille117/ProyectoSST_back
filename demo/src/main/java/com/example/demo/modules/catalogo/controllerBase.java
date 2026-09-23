package com.example.demo.modules.catalogo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

public abstract class controllerBase<T extends entityBase> {

    // Retorna la instancia del servicio específico que maneja la entidad.
    protected abstract serviceBase<T> getService();

    @GetMapping
    public List<T> listar(@RequestParam(name = "activos", required = false) Boolean activos) {
        if (activos != null) {
            return getService().listarPorEstado(activos);
        }
        return getService().listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> buscarPorId(@PathVariable("id") Long id) {
        return getService().buscarPorId(id)
                .map(ResponseEntity::ok) // Si lo encuentra, responde con 200 OK y el objeto
                .orElse(ResponseEntity.notFound().build()); // Si no lo encuentra, responde con 404 Not Found
    }

    @PostMapping
    @PreAuthorize("@permissionService.canCreateCatalog(authentication)")
    public ResponseEntity<T> crear(@RequestBody T entidad) {
        entidad.setId(null);
        entidad.setEstado(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().guardar(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEditCatalog(authentication)")
    public ResponseEntity<T> actualizar(@PathVariable("id") Long id, @RequestBody T entidadDetalles) {
        return getService().actualizar(id, entidadDetalles.getNombre())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.canEditCatalog(authentication)")
    public ResponseEntity<T> cambiarEstado(@PathVariable("id") Long id) {
        return getService().cambiarEstado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
