package com.example.demo.modules.farmacia.medicamentoLog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/medicamentoLog")
@RequiredArgsConstructor
public class medicamentoLogController {
    private final medicamentoLogService service;

    @GetMapping("/conteoCatalogosFarmacia")
    public Map<String, Long> getConteosCatalogos(){
        return service.obtenerConteoCatalogosFarmacia();
    }

    @PostMapping
    public ResponseEntity<medicamentoLogDTOs.Response> crear(@Valid @RequestBody medicamentoLogDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<medicamentoLogDTOs.MedicamentoLogResponse>> obtenerTodos(
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerTodos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<medicamentoLogDTOs.MedicamentoLogResponse>> buscar(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "marcaId", required = false) Long marcaId,
            @RequestParam(name = "presentacionId", required = false) Long presentacionId,
            @RequestParam(name = "viaAdminId", required = false) Long viaAdminId,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.buscar(nombre, marcaId, presentacionId, viaAdminId, activo));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<medicamentoLogDTOs.MedicamentoLogBusquedaResponse> buscar(
            @PathVariable("id") Long id,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.buscarPorId(id, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<medicamentoLogDTOs.Response> obtenerPorId(
            @PathVariable("id") Long id,
            @RequestParam(name = "activo", defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(service.obtenerPorId(id, activo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<medicamentoLogDTOs.Response> actualizar(@PathVariable("id") Long id, @Valid @RequestBody medicamentoLogDTOs.Request request) { return ResponseEntity.ok(service.actualizar(id, request)); }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cambiarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}