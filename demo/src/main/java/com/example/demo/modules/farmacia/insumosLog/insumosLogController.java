package com.example.demo.modules.farmacia.insumosLog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/insumosLog")
@RequiredArgsConstructor
public class insumosLogController {
    private final insumosLogService service;

    @PostMapping
    public ResponseEntity<insumosLogDTOs.Response> crear(@Valid @RequestBody insumosLogDTOs.Request request) { return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED); }

    @GetMapping
    public ResponseEntity<List<insumosLogDTOs.ListadoResponse>> obtenerTodos(
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerTodos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<insumosLogDTOs.ListadoResponse>> buscar(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "marcaId", required = false) Long marcaId,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.buscar(nombre, marcaId, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<insumosLogDTOs.Response> obtenerPorId(@PathVariable Long id) { return ResponseEntity.ok(service.obtenerPorId(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<insumosLogDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody insumosLogDTOs.Request request) { return ResponseEntity.ok(service.actualizar(id, request)); }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cambiarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}