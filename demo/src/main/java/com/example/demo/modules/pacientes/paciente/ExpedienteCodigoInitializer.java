package com.example.demo.modules.pacientes.paciente;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(20)
public class ExpedienteCodigoInitializer implements CommandLineRunner {

    private final ExpedienteRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        List<ExpedienteEntity> expedientes = repository.findAll();
        expedientes.forEach(ExpedienteEntity::actualizarCodigo);
        repository.saveAll(expedientes);
    }
}