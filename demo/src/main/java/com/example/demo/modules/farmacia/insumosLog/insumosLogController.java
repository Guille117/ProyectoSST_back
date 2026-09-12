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
    public ResponseEntity<List<insumosLogDTOs.Response>> obtenerTodos() { return ResponseEntity.ok(service.obtenerTodos()); }

    @GetMapping("/buscar")
    public ResponseEntity<List<insumosLogDTOs.Response>> buscar(@RequestParam String nombre) { return ResponseEntity.ok(service.buscarPorNombre(nombre)); }

    @GetMapping("/{id}")
    public ResponseEntity<insumosLogDTOs.Response> obtenerPorId(@PathVariable Long id) { return ResponseEntity.ok(service.obtenerPorId(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<insumosLogDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody insumosLogDTOs.Request request) { return ResponseEntity.ok(service.actualizar(id, request)); }
}