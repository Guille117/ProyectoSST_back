package com.example.demo.modules.pacientes.paciente;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService service;

    @GetMapping
    public ResponseEntity<List<PacienteDTOs.ListadoResponse>> listar(
            @RequestParam(name = "estado", required = false) Boolean estado) {
        return ResponseEntity.ok(service.listar(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteDTOs.ListadoResponse>> buscar(
            @RequestParam("texto") String texto) {
        return ResponseEntity.ok(service.buscar(texto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTOs.DetalleResponse> obtenerDetalle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PacienteDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestPart("request") PacienteDTOs.ActualizacionRequest request,
            @RequestPart(value = "archivoReferencia", required = false) MultipartFile archivoReferencia) {
        return ResponseEntity.ok(service.actualizar(id, request, archivoReferencia));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PacienteDTOs.Response> crear(@Valid @RequestBody PacienteDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PacienteDTOs.Response> crearConArchivo(
            @Valid @RequestPart("request") PacienteDTOs.Request request,
            @RequestPart(value = "archivoReferencia", required = false) MultipartFile archivoReferencia,
            @RequestPart(value = "referenciaArchivos", required = false) List<MultipartFile> referenciaArchivos,
            @RequestPart(value = "autorizacionArchivos", required = false) List<MultipartFile> autorizacionArchivos,
            @RequestPart(value = "responsabilidadArchivos", required = false) List<MultipartFile> responsabilidadArchivos) {
        return new ResponseEntity<>(service.crear(request, archivoReferencia, referenciaArchivos,
                autorizacionArchivos, responsabilidadArchivos), HttpStatus.CREATED);
    }
}