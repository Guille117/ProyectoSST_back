package com.example.demo.modules.enfermeria.tratamientos;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enfermeria/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadMedicamentoController {

    private final DisponibilidadMedicamentoService service;

    @GetMapping
    public ResponseEntity<List<disponibilidadMedicamentoDTOs.Response>> obtener(
            @RequestParam(name = "episodioId") Long episodioId) {
        return ResponseEntity.ok(service.disponibilidadPorEpisodio(episodioId));
    }
}
