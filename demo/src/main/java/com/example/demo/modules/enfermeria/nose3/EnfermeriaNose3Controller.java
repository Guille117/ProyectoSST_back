package com.example.demo.modules.enfermeria.nose3;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria-nose3")
@RequiredArgsConstructor
public class EnfermeriaNose3Controller {

    private final EnfermeriaNose3Service service;

    @PostMapping
    public ResponseEntity<EnfermeriaNose3DTOs.Response> crear(@Valid @RequestBody EnfermeriaNose3DTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EnfermeriaNose3DTOs.Response>> obtenerTodos(@RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<EnfermeriaNose3DTOs.Response>> buscarPorNombre(@RequestParam String nombre, @RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnfermeriaNose3DTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnfermeriaNose3DTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody EnfermeriaNose3DTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
