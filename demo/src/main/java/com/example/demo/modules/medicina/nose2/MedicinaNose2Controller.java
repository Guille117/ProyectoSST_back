package com.example.demo.modules.medicina.nose2;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicina-nose2")
@RequiredArgsConstructor
public class MedicinaNose2Controller {

    private final MedicinaNose2Service service;

    @PostMapping
    public ResponseEntity<MedicinaNose2DTOs.Response> crear(@Valid @RequestBody MedicinaNose2DTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicinaNose2DTOs.Response>> obtenerTodos(@RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MedicinaNose2DTOs.Response>> buscarPorNombre(@RequestParam String nombre, @RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicinaNose2DTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicinaNose2DTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody MedicinaNose2DTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
