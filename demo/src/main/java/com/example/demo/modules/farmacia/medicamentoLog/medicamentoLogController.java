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
    public ResponseEntity<List<medicamentoLogDTOs.Response>> obtenerTodos() { return ResponseEntity.ok(service.obtenerTodos()); }

    @GetMapping("/buscar")
    public ResponseEntity<List<medicamentoLogDTOs.Response>> buscar(@RequestParam String nombre) { return ResponseEntity.ok(service.buscarPorNombre(nombre)); }

    @GetMapping("/{id}")
    public ResponseEntity<medicamentoLogDTOs.Response> obtenerPorId(@PathVariable Long id) { return ResponseEntity.ok(service.obtenerPorId(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<medicamentoLogDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody medicamentoLogDTOs.Request request) { return ResponseEntity.ok(service.actualizar(id, request)); }
}