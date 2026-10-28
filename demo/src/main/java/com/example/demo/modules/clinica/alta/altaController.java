package com.example.demo.modules.clinica.alta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/altas")
@RequiredArgsConstructor
public class altaController {

    private final altaService service;

    @PostMapping
    public ResponseEntity<altaDTOs.Response> crear(
            @Valid @RequestBody altaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<altaDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId));
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<altaDTOs.RecienteResponse>> recientes(
            @RequestParam(name = "horas", defaultValue = "48") int horas) {
        return ResponseEntity.ok(service.recientes(horas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<altaDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<altaDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody altaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable("id") Long id) {
        service.anular(id);
        return ResponseEntity.noContent().build();
    }
}
