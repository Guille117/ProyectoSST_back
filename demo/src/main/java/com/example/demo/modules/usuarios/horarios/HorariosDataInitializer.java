package com.example.demo.modules.usuarios.horarios;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class HorariosDataInitializer implements CommandLineRunner {

    private static final String HORARIO_SIN_LIMITE = "Horario Libre";

    private final HorarioRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.findByNombreIgnoreCase(HORARIO_SIN_LIMITE).isPresent()) {
            return;
        }

        // esRotativo=true hace que AuthService.validarHorarioLaboral no aplique ninguna restricción de tiempo.
        HorarioEntity horario = HorarioEntity.builder()
                .codigo(generarCodigoHorario())
                .nombre(HORARIO_SIN_LIMITE)
                .esRotativo(true)
                .estado(true)
                .build();
        repository.save(horario);
    }

    private String generarCodigoHorario() {
        long total = repository.count();
        return String.format("HOR-%02d", total + 1);
    }
}
