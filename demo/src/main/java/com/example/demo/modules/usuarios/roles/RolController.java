package com.example.demo.modules.usuarios.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService service;

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

    @GetMapping("/modulos")
    public ResponseEntity<List<RolDTOs.ModuloResponse>> obtenerModulos() {
        return ResponseEntity.ok(service.obtenerModulosJerarquicos());
    }
}
