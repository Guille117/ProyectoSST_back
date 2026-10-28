package com.example.demo.modules.clinica.diagnostico;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnostico")
@RequiredArgsConstructor
public class diagnosticoController {

    private final diagnosticoService service;

    @PostMapping
    public ResponseEntity<diagnosticoDTOs.Response> crear(@Valid @RequestBody diagnosticoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<diagnosticoDTOs.Response>> obtenerTodos(
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerTodos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<diagnosticoDTOs.Response>> buscar(
            @RequestParam(name = "episodioId", required = false) Long episodioId,
            @RequestParam(name = "tipoDiagnostico", required = false) TipoDiagnostico tipoDiagnostico,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        if (tipoDiagnostico != null) {
            return ResponseEntity.ok(service.buscarPorTipo(tipoDiagnostico, activo));
        }
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<diagnosticoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<diagnosticoDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                              @Valid @RequestBody diagnosticoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<diagnosticoDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }
}
