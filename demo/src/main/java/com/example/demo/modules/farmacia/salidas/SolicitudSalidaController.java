package com.example.demo.modules.farmacia.salidas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudesSalida")
@RequiredArgsConstructor
public class SolicitudSalidaController {

    private final SolicitudSalidaService service;

    @PostMapping
    public ResponseEntity<SolicitudSalidaDTOs.Response> crearSolicitudMedicamentos(
            @Valid @RequestBody SolicitudSalidaDTOs.SolicitudMedicamentoRequest request) {
        return new ResponseEntity<>(service.crearSolicitudMedicamentos(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SolicitudSalidaDTOs.ResumenResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoSolicitudSalida estado,
            @RequestParam(name = "texto", required = false) String texto) {
        return ResponseEntity.ok(service.listar(estado, texto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudSalidaDTOs.Response> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<SolicitudSalidaDTOs.HistorialResponse>> historial(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    @PostMapping("/{id}/entregas")
    public ResponseEntity<SolicitudSalidaDTOs.Response> entregar(
            @PathVariable("id") Long id,
            @Valid @RequestBody SolicitudSalidaDTOs.EntregaRequest request) {
        return ResponseEntity.ok(service.entregar(id, request));
    }

    @PostMapping("/{id}/devoluciones")
    public ResponseEntity<SolicitudSalidaDTOs.Response> devolver(
            @PathVariable("id") Long id,
            @Valid @RequestBody SolicitudSalidaDTOs.DevolucionRequest request) {
        return ResponseEntity.ok(service.devolver(id, request));
    }
}
