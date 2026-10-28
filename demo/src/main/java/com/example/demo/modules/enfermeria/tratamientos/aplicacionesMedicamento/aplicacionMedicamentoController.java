package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria/aplicaciones-medicamento")
@RequiredArgsConstructor
public class aplicacionMedicamentoController {

    private final aplicacionMedicamentoService service;

    @PostMapping
    public ResponseEntity<aplicacionMedicamentoDTOs.Response> crear(
            @Valid @RequestBody aplicacionMedicamentoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<aplicacionMedicamentoDTOs.Response>> buscar(
            @RequestParam(name = "indicacionId", required = false) Long indicacionId,
            @RequestParam(name = "episodioId", required = false) Long episodioId) {
        return ResponseEntity.ok(service.buscar(indicacionId, episodioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<aplicacionMedicamentoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<aplicacionMedicamentoDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody aplicacionMedicamentoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
