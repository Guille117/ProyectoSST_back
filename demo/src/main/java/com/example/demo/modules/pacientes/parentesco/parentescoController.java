package com.example.demo.modules.pacientes.parentesco;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parentescos")
@RequiredArgsConstructor
public class parentescoController {

    private final parentescoService service;

    @PostMapping
    public ResponseEntity<parentescoDTOs.Response> crear(@Valid @RequestBody parentescoDTOs.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<parentescoDTOs.Response>> obtenerTodos(
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<parentescoDTOs.Response>> buscarPorNombre(@RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<parentescoDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
        public ResponseEntity<parentescoDTOs.Response> actualizar(
                @PathVariable("id") Long id,
            @Valid @RequestBody parentescoDTOs.Request request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

        @PatchMapping("/{id}")
        public ResponseEntity<parentescoDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
            return service.alternarEstado(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }
}