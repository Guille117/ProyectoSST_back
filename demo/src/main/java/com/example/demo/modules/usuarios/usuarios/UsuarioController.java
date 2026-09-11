package com.example.demo.modules.usuarios.usuarios;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<UsuarioDTOs.Response> crear(@Valid @RequestBody UsuarioDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTOs.Response>> obtenerTodos(@RequestParam(required = false, name = "activos") Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTOs.Response>> buscar(
            @RequestParam(name = "criterio") String criterio,
            @RequestParam(required = false, name = "activos") Boolean activos) {
        return ResponseEntity.ok(service.buscarPorCriterio(criterio, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTOs.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTOs.Response> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTOs.UpdateRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
