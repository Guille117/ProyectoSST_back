package com.example.demo.modules.usuarios.roles;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService service;

    @PostMapping
    public ResponseEntity<RolDTOs.Response> crear(@Valid @RequestBody RolDTOs.Request request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RolDTOs.Response>> obtenerTodos(@RequestParam(required = false, name = "activos") Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<RolDTOs.Response>> buscar(
            @RequestParam(name = "criterio") String criterio,
            @RequestParam(required = false, name = "activos") Boolean activos) {
        return ResponseEntity.ok(service.buscarPorCriterio(criterio, activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolDTOs.Response> actualizar(
            @PathVariable("id") Long id, 
            @Valid @RequestBody RolDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/modulos")
    public ResponseEntity<List<RolDTOs.ModuloResponse>> obtenerModulos() {
        return ResponseEntity.ok(service.obtenerModulosJerarquicos());
    }
}
