package com.example.demo.modules.usuarios.horarios;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Order(3)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class HorariosDataInitializer implements CommandLineRunner {

    private static final String NOMBRE_SIN_LIMITE = "Sin limite";

    private final HorarioRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        Optional<HorarioEntity> existente = repository.findByNombreIgnoreCase(NOMBRE_SIN_LIMITE)
                .or(() -> repository.findByNombreIgnoreCase("Horario Libre"));
        if (existente.isPresent()) {
            return;
        }

        HorarioEntity horario = HorarioEntity.builder()
                .codigo(generarCodigoHorario())
                .nombre(NOMBRE_SIN_LIMITE)
                .estado(true)
                .esRotativo(false)
                .build();

        for (DiaSemana dia : DiaSemana.values()) {
            horario.getSemanalDetalles().add(HorarioSemanalDetalleEntity.builder()
                    .horario(horario)
                    .diaSemana(dia)
                    .horaEntrada(LocalTime.MIN)
                    .horaSalida(LocalTime.MAX)
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
