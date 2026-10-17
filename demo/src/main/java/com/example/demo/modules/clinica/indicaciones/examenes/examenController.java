package com.example.demo.modules.clinica.indicaciones.examenes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/examenes")
@RequiredArgsConstructor
public class examenController {

    private final examenService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<examenDTOs.Response> crear(
            @Valid @RequestPart("request") examenDTOs.Request request,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {
        return new ResponseEntity<>(service.crear(request, archivos), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<examenDTOs.Response>> obtenerTodos(
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoExamen estado) {
        return ResponseEntity.ok(service.obtenerTodos(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<examenDTOs.Response>> buscar(
            @RequestParam(name = "episodioId") Long episodioId,
            @RequestParam(name = "estado", defaultValue = "ACTIVO") EstadoExamen estado) {
        return ResponseEntity.ok(service.buscarPorEpisodio(episodioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<examenDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<examenDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestPart("request") examenDTOs.Request request,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {
        return ResponseEntity.ok(service.actualizar(id, request, archivos));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<examenDTOs.Response> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestParam(name = "estado") EstadoExamen estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}/archivos/{archivoId}")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable("id") Long id,
                                                @PathVariable("archivoId") Long archivoId) {
        service.eliminarArchivo(id, archivoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
