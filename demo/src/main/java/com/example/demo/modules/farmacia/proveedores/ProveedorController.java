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
    public ResponseEntity<List<ProveedorDTOs.Response>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
}
