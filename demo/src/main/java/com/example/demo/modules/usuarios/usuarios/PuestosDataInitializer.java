package com.example.demo.modules.usuarios.usuarios;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(2)
public class PuestosDataInitializer implements CommandLineRunner {

    private final PuestoRepository puestoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (puestoRepository.count() > 0) {
            return;
        }

        crearPuesto("PUE-01", "Director");
        crearPuesto("PUE-02", "Enfermera");
        crearPuesto("PUE-03", "Doctor");
        crearPuesto("PUE-04", "Encargada de farmacia");
        crearPuesto("PUE-05", "Administrador");
    }

    private void crearPuesto(String codigo, String nombre) {
        PuestoEntity puesto = PuestoEntity.builder()
                .codigo(codigo)
                .nombre(nombre)
                .estado(true)
                .build();
        puestoRepository.save(puesto);
    }
}
