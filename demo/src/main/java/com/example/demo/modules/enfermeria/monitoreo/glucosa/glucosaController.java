package com.example.demo.modules.enfermeria.monitoreo.glucosa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria/glucosa")
@RequiredArgsConstructor
public class glucosaController {

    private final glucosaService service;

    @PostMapping
    public ResponseEntity<glucosaDTOs.Response> crear(@Valid @RequestBody glucosaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<glucosaDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<glucosaDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<glucosaDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody glucosaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
