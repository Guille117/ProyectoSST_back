package com.example.demo.modules.clinica.indicaciones.medicamentos;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicamentos")
@RequiredArgsConstructor
public class indicacionMedicamentoController {

    private final indicacionMedicamentoService service;

    @PostMapping
    public ResponseEntity<indicacionMedicamentoDTOs.Response> crear(@Valid @RequestBody indicacionMedicamentoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<indicacionMedicamentoDTOs.Response>> obtenerTodos(
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoIndicacionMedicamento estado) {
        return ResponseEntity.ok(service.obtenerTodos(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<indicacionMedicamentoDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoIndicacionMedicamento estado) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<indicacionMedicamentoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<indicacionMedicamentoDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody indicacionMedicamentoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<indicacionMedicamentoDTOs.Response> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") EstadoIndicacionMedicamento estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
