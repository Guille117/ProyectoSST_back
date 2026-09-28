package com.example.demo.modules.usuarios.puesto;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class PuestosDataInitializer implements CommandLineRunner {

    private static final List<String> PUESTOS_DEL_SISTEMA = List.of("Administrador", "Médico");

    private final puestoRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        for (String nombre : PUESTOS_DEL_SISTEMA) {
                puestoEntity puesto = repository.findByNombreIgnoreCase(nombre)
                    .or(() -> nombre.equals("Médico") ? repository.findByNombreIgnoreCase("Medico") : java.util.Optional.empty())
                    .orElseGet(puestoEntity::new);
            puesto.setNombre(nombre);
            puesto.setEstado(true);
            repository.save(puesto);
        }
    }
}