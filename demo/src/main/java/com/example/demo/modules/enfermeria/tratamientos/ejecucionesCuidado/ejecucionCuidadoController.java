package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria/ejecuciones-cuidado")
@RequiredArgsConstructor
public class ejecucionCuidadoController {

    private final ejecucionCuidadoService service;

    @PostMapping
    public ResponseEntity<ejecucionCuidadoDTOs.Response> crear(
            @Valid @RequestBody ejecucionCuidadoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ejecucionCuidadoDTOs.Response>> buscar(
            @RequestParam(name = "cuidadoId", required = false) Long cuidadoId,
            @RequestParam(name = "episodioId", required = false) Long episodioId) {
        return ResponseEntity.ok(service.buscar(cuidadoId, episodioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ejecucionCuidadoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ejecucionCuidadoDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody ejecucionCuidadoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
