package com.example.demo.modules.clinica.historiaClinica;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historiaClinica")
@RequiredArgsConstructor
public class historiaClinicaController {

    private final historiaClinicaService service;

    @PostMapping
    public ResponseEntity<historiaClinicaDTOs.Response> crear(@Valid @RequestBody historiaClinicaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<historiaClinicaDTOs.Response>> obtenerTodos(
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerTodos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<historiaClinicaDTOs.Response>> buscarPorEpisodio(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<historiaClinicaDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<historiaClinicaDTOs.Response> actualizar(@PathVariable("id") Long id,
                                                                  @Valid @RequestBody historiaClinicaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<historiaClinicaDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }
}
