package com.example.demo.modules.farmacia.bajas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bajas")
@RequiredArgsConstructor
public class BajaController {

    private final BajaService service;

    @PostMapping
    public ResponseEntity<BajaDTOs.Response> registrar(@Valid @RequestBody BajaDTOs.Request request) {
        return new ResponseEntity<>(service.registrar(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BajaDTOs.ListadoResponse>> listar(
            @RequestParam(name = "tipo", required = false) String tipo) {
        return ResponseEntity.ok(service.listar(tipo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revertir(@PathVariable("id") Long id) {
        service.revertir(id);
        return ResponseEntity.noContent().build();
    }
}
