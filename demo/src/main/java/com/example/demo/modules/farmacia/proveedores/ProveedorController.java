package com.example.demo.modules.farmacia.proveedores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService service;

    @PostMapping
    public ResponseEntity<ProveedorDTOs.Response> crear(@Valid @RequestBody ProveedorDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProveedorDTOs.Response>> obtenerTodos(@RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTOs.Response>> buscarPorNombre(
            @RequestParam String nombre,
            @RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
