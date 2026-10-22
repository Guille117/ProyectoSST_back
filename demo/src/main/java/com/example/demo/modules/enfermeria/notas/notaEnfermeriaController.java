package com.example.demo.modules.enfermeria.notas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria/notas")
@RequiredArgsConstructor
public class notaEnfermeriaController {

    private final notaEnfermeriaService service;

    @PostMapping
    public ResponseEntity<notaEnfermeriaDTOs.Response> crear(@Valid @RequestBody notaEnfermeriaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<notaEnfermeriaDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<notaEnfermeriaDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<notaEnfermeriaDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody notaEnfermeriaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
