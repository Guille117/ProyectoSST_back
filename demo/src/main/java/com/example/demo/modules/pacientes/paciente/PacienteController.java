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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PacienteDTOs.Response> crear(@Valid @RequestBody PacienteDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PacienteDTOs.Response> crearConArchivo(
            @Valid @RequestPart("request") PacienteDTOs.Request request,
            @RequestPart(value = "archivoReferencia", required = false) MultipartFile archivoReferencia) {
        return new ResponseEntity<>(service.crear(request, archivoReferencia), HttpStatus.CREATED);
    }
}