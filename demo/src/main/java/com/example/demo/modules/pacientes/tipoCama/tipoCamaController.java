package com.example.demo.modules.pacientes.tipoCama;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/tipos-cama", "/api/v1/tiposCama"})
@RequiredArgsConstructor
public class tipoCamaController {

    private final tipoCamaService service;

    @PostMapping
    @PreAuthorize("@permissionService.canCreate(authentication, 'TIPOS-CAMA') or @permissionService.canCreateCatalog(authentication)")
    public ResponseEntity<tipoCamaEntity> crear(@Valid @RequestBody tipoCamaEntity request) {
        request.setId(null);
        request.setEstado(true);
        return new ResponseEntity<>(service.guardar(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<tipoCamaEntity>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(activos == null ? service.listarTodos() : service.listarPorEstado(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<tipoCamaEntity>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<tipoCamaEntity> obtenerPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'TIPOS-CAMA') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<tipoCamaEntity> actualizar(@PathVariable("id") Long id, @RequestBody tipoCamaEntity request) {
        return ResponseEntity.ok(service.actualizarConDescripcion(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'TIPOS-CAMA') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<tipoCamaEntity> cambiarEstado(@PathVariable("id") Long id) {
        return service.cambiarEstado(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}