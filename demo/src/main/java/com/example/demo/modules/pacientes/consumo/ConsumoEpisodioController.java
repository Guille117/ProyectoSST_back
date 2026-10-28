package com.example.demo.modules.pacientes.consumo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/episodios")
@RequiredArgsConstructor
public class ConsumoEpisodioController {

    private final ConsumoEpisodioService service;

    @GetMapping("/{episodioId}/consumo")
    public ResponseEntity<ConsumoEpisodioDTOs.Response> obtenerConsumo(
            @PathVariable("episodioId") Long episodioId) {
        return ResponseEntity.ok(service.obtenerConsumo(episodioId));
    }
}
