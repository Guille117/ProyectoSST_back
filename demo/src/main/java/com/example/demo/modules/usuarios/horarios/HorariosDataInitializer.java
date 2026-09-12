package com.example.demo.modules.usuarios.horarios;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HorariosDataInitializer implements CommandLineRunner {

    private static final String NOMBRE_SIN_LIMITE = "Sin limite";

    private final HorarioRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        Optional<HorarioEntity> existente = repository.findByNombreIgnoreCase(NOMBRE_SIN_LIMITE)
                .or(() -> repository.findByNombreIgnoreCase("Horario Libre"));

        HorarioEntity horario = existente.orElseGet(() -> HorarioEntity.builder()
                .codigo(generarCodigoHorario())
                .nombre(NOMBRE_SIN_LIMITE)
                .estado(true)
                .build());

        horario.setNombre(NOMBRE_SIN_LIMITE);
        horario.setEsRotativo(false);

        LocalTime entrada = LocalTime.of(0, 0, 0);
        LocalTime salida = LocalTime.of(23, 59, 59);

        if (horario.getSemanalDetalles() == null) {
            horario.setSemanalDetalles(new ArrayList<>());
        } else {
            horario.getSemanalDetalles().clear();
        }

        for (DiaSemana dia : DiaSemana.values()) {
            horario.getSemanalDetalles().add(HorarioSemanalDetalleEntity.builder()
                    .horario(horario)
                    .diaSemana(dia)
                    .horaEntrada(entrada)
                    .horaSalida(salida)
                    .activo(true)
                    .build());
        }

        repository.save(horario);
    }

    private String generarCodigoHorario() {
        long total = repository.count();
        return String.format("HOR-%02d", total + 1);
    }
}
