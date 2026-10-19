package com.example.demo.modules.clinica.procedimientoMedico;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procedimientosMedicos")
@RequiredArgsConstructor
public class procedimientoMedicoController {

    private final procedimientoMedicoService service;

    @PostMapping
    public ResponseEntity<procedimientoMedicoDTOs.Response> crear(
            @Valid @RequestBody procedimientoMedicoDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<procedimientoMedicoDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId));
    }

    @GetMapping("/recursos")
    public ResponseEntity<List<procedimientoMedicoDTOs.RecursoDisponibleResponse>> buscarRecursos(
            @RequestParam(name = "tipo") TipoRecursoProcedimiento tipo,
            @RequestParam(name = "nombre", required = false) String nombre) {
        return ResponseEntity.ok(service.buscarRecursos(tipo, nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<procedimientoMedicoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<procedimientoMedicoDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody procedimientoMedicoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
