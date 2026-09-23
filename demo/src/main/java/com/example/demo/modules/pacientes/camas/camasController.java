package com.example.demo.modules.pacientes.camas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camas")
@RequiredArgsConstructor
public class camasController {
    private final camasService service;

    @PostMapping
    @PreAuthorize("@permissionService.canCreate(authentication, 'CAMAS') or @permissionService.canCreateCatalog(authentication)")
    public ResponseEntity<camasDTOs.Response> crear(@Valid @RequestBody camasDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<camasDTOs.ListResponse>> obtenerTodos(
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<camasDTOs.ListResponse>> buscar(
            @RequestParam("texto") String texto,
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscar(texto, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<camasDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/{id}/referencias")
    public ResponseEntity<camasDTOs.ReferenciasResponse> obtenerReferencias(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerReferencias(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'CAMAS') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<camasDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                          @Valid @RequestBody camasDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'CAMAS') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<camasDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("@permissionService.canEdit(authentication, 'CAMAS') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<Void> cambiarEstadoCama(@PathVariable("id") Long id, @RequestParam("estado") EstadoCama estado) {
        service.cambiarEstadoCama(id, estado);
        return ResponseEntity.ok().build();
    }   

}