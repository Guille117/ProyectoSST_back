package com.example.demo.modules.clinica.evolucion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evolucion")
@RequiredArgsConstructor
public class evolucionController {

    private final evolucionService service;

    @PostMapping
    public ResponseEntity<evolucionDTOs.Response> crear(
            @Valid @RequestBody evolucionDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<evolucionDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "estado", defaultValue = "ACTIVA") EstadoEvolucion estado) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<evolucionDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<evolucionDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody evolucionDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
