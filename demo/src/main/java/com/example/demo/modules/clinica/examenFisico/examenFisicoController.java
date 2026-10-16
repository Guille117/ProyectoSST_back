package com.example.demo.modules.clinica.examenFisico;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/examenFisico")
@RequiredArgsConstructor
public class examenFisicoController {

    private final examenFisicoService service;

    @PostMapping
    public ResponseEntity<examenFisicoDTOs.Response> crear(@Valid @RequestBody examenFisicoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<examenFisicoDTOs.Response>> obtenerTodos(
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerTodos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<examenFisicoDTOs.Response>> buscar(
            @RequestParam(name = "episodioId", required = false) Long episodioId,
            @RequestParam(name = "parteCuerpoId", required = false) Long parteCuerpoId,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        if (parteCuerpoId != null) {
            return ResponseEntity.ok(service.buscarPorParteCuerpo(parteCuerpoId, activo));
        }
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<examenFisicoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<examenFisicoDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                               @Valid @RequestBody examenFisicoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<examenFisicoDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }
}
