package com.example.demo.modules.clinica.indicaciones.indicacionMedica;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/indicacionMedica")
@RequiredArgsConstructor
public class indicacionMedicaController {

    private final indicacionMedicaService service;

    @PostMapping
    public ResponseEntity<indicacionMedicaDTOs.Response> crear(@Valid @RequestBody indicacionMedicaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<indicacionMedicaDTOs.Response>> obtenerTodos(
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoIndicacionMedica estado) {
        return ResponseEntity.ok(service.obtenerTodos(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<indicacionMedicaDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoIndicacionMedica estado) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<indicacionMedicaDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<indicacionMedicaDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody indicacionMedicaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<indicacionMedicaDTOs.Response> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") EstadoIndicacionMedica estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
