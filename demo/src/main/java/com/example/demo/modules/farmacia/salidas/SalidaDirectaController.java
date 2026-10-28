package com.example.demo.modules.farmacia.salidas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Salidas directas de Farmacia: registran la salida de productos ligada al
 * episodio y descuentan el inventario en el momento. No son solicitudes.
 */
@RestController
@RequestMapping("/api/v1/salidasDirectas")
@RequiredArgsConstructor
public class SalidaDirectaController {

    private final SolicitudSalidaService service;

    @PostMapping
    public ResponseEntity<SolicitudSalidaDTOs.Response> crear(
            @Valid @RequestBody SolicitudSalidaDTOs.SalidaDirectaRequest request) {
        return new ResponseEntity<>(service.crearDirecta(request), HttpStatus.CREATED);
    }
}
