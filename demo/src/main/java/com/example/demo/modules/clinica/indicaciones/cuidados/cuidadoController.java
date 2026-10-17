package com.example.demo.modules.clinica.indicaciones.cuidados;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuidados")
@RequiredArgsConstructor
public class cuidadoController {

    private final cuidadoService service;

    @PostMapping
    public ResponseEntity<cuidadoDTOs.Response> crear(@Valid @RequestBody cuidadoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<cuidadoDTOs.Response>> obtenerTodos(
            @RequestParam(name = "estado", defaultValue = "EN_PROCESO") EstadoCuidado estado) {
        return ResponseEntity.ok(service.obtenerTodos(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<cuidadoDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "estado", defaultValue = "EN_PROCESO") EstadoCuidado estado) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<cuidadoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<cuidadoDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                           @Valid @RequestBody cuidadoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<cuidadoDTOs.Response> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") EstadoCuidado estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
