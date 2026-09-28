package com.example.demo.modules.pacientes.habitaciones;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class habitacionesController {

    private final habitacionesService service;

    @PostMapping
    public ResponseEntity<habitacionesEntity> crear(@Valid @RequestBody habitacionesEntity request) {
        request.setId(null);
        request.setEstado(true);
        return new ResponseEntity<>(service.guardar(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<habitacionesEntity>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(activos == null ? service.listarTodos() : service.listarPorEstado(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<habitacionesEntity>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<habitacionesEntity> obtenerPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<habitacionesEntity> actualizar(@PathVariable("id") Long id, @RequestBody habitacionesEntity request) {
        return ResponseEntity.ok(service.actualizarConDescripcion(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<habitacionesEntity> cambiarEstado(@PathVariable("id") Long id) {
        return service.cambiarEstado(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}