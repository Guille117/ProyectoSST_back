package com.example.demo.modules.paciente.camas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camas")
@RequiredArgsConstructor
public class CamaController {

    private final CamaService service;

    @PostMapping
    public ResponseEntity<CamaDTOs.Response> crear(@Valid @RequestBody CamaDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CamaDTOs.Response>> obtenerTodos(@RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CamaDTOs.Response>> buscarPorNombre(@RequestParam String nombre, @RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CamaDTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CamaDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody CamaDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
