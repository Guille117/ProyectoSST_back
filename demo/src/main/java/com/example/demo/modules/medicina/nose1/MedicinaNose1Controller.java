package com.example.demo.modules.medicina.nose1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicina-nose1")
@RequiredArgsConstructor
public class MedicinaNose1Controller {

    private final MedicinaNose1Service service;

    @PostMapping
    public ResponseEntity<MedicinaNose1DTOs.Response> crear(@Valid @RequestBody MedicinaNose1DTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicinaNose1DTOs.Response>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MedicinaNose1DTOs.Response>> buscarPorNombre(@RequestParam(name = "nombre") String nombre, @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicinaNose1DTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicinaNose1DTOs.Response> actualizar(@PathVariable("id") Long id, @Valid @RequestBody MedicinaNose1DTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
