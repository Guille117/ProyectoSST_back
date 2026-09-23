package com.example.demo.modules.farmacia.devoluciones;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devoluciones")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService service;

    @PostMapping
    @PreAuthorize("@permissionService.canCreate(authentication, 'DEVOLUCIONES')")
    public ResponseEntity<DevolucionDTOs.Response> crear(@Valid @RequestBody DevolucionDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DevolucionDTOs.Response>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<DevolucionDTOs.Response>> buscarPorNombre(@RequestParam(name = "nombre") String nombre, @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevolucionDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'DEVOLUCIONES')")
    public ResponseEntity<DevolucionDTOs.Response> actualizar(@PathVariable("id") Long id, @Valid @RequestBody DevolucionDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'DEVOLUCIONES')")
    public ResponseEntity<Void> actualizarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
