package com.example.demo.modules.bitacora;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/bitacora")
@RequiredArgsConstructor
public class BitacoraController {

    private static final Set<TipoBitacora> TIPOS_GENERAL = Set.of(
            TipoBitacora.LOGIN, TipoBitacora.USUARIO, TipoBitacora.GENERAL, TipoBitacora.PACIENTE);

    private final BitacoraService bitacoraService;

    @GetMapping("/pacientes/{pacienteId}")
    public List<BitacoraDTOs.BitacoraResponse> porPaciente(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "500") int limite) {
        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fin = hasta != null ? hasta.atTime(LocalTime.MAX) : null;
        return bitacoraService.listar(null, pacienteId, null, inicio, fin, limite);
    }

    @GetMapping("/general")
    public List<BitacoraDTOs.BitacoraResponse> general(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "200") int limite) {
        return bitacoraService.listar(TIPOS_GENERAL, null, null, inicio(desde), fin(hasta), limite);
    }

    private LocalDateTime inicio(LocalDate desde) {
        return (desde != null ? desde : LocalDate.now().minusMonths(1)).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return (hasta != null ? hasta : LocalDate.now()).atTime(LocalTime.MAX);
    }
}
