package com.example.demo.modules.pacientes.paciente;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(20)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class ExpedienteCodigoInitializer implements CommandLineRunner {

    private final ExpedienteRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        List<ExpedienteEntity> modificados = new ArrayList<>();
        for (ExpedienteEntity expediente : repository.findAll()) {
            if (expediente.getCodigo() == null || expediente.getCodigo().isBlank()) {
                expediente.actualizarCodigo();
                modificados.add(expediente);
            }
        }
        if (!modificados.isEmpty()) {
            repository.saveAll(modificados);
        }
    }
}
