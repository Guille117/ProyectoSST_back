package com.example.demo.modules.pacientes.institucion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instituciones")
@RequiredArgsConstructor
public class institucionController {

    private final institucionService service;

    public record ConteoCatalogoResponse(String tabla, long total) {}

    @PostMapping
    public ResponseEntity<institucionEntity> crear(@Valid @RequestBody institucionEntity request) {
        request.setId(null);
        request.setEstado(true);
        return new ResponseEntity<>(service.guardar(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<institucionEntity>> obtenerTodos(
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(activos == null ? service.listarTodos() : service.listarPorEstado(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<institucionEntity>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/conteo-catalogos")
    public ResponseEntity<List<ConteoCatalogoResponse>> contarCatalogosActivos() {
        return ResponseEntity.ok(List.of(
                new ConteoCatalogoResponse("instituciones", service.contarActivas())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<institucionEntity> obtenerPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<institucionEntity> actualizar(
            @PathVariable("id") Long id, @RequestBody institucionEntity request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<institucionEntity> cambiarEstado(@PathVariable("id") Long id) {
        return service.cambiarEstado(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}