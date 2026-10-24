package com.example.demo.modules.pacientes.expediente;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class ExpedienteController {

    private final ExpedienteService service;

    @GetMapping("/{pacienteId}/episodios")
    public ResponseEntity<List<ExpedienteDTOs.EpisodioResumenResponse>> listarEpisodios(
            @PathVariable("pacienteId") Long pacienteId) {
        return ResponseEntity.ok(service.listarEpisodios(pacienteId));
    }
}
