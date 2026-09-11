package com.example.demo.modules.usuarios.usuarios;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/puestos")
@RequiredArgsConstructor
public class PuestoController {

    private final PuestoService service;

    @GetMapping
    public ResponseEntity<List<PuestoDTOs.Response>> obtenerTodos(@RequestParam(required = false, name = "activos") Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PuestoDTOs.Response>> obtenerActivos() {
        return ResponseEntity.ok(service.obtenerTodos(true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuestoDTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PuestoDTOs.Response>> buscarPorNombre(
            @RequestParam String nombre,
            @RequestParam(required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre, activos));
    }
}
