package com.example.demo.modules.usuarios.puesto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PuestosDataInitializerTest {

    @Mock
    private puestoRepository repository;

    @Test
    void run_creaLosPuestosDelSistemaYReactivaLosExistentes() {
        puestoEntity administrador = new puestoEntity();
        administrador.setNombre("Administrador");
        administrador.setEstado(false);
        puestoEntity medico = new puestoEntity();
        medico.setId(2L);
        medico.setNombre("Medico");
        medico.setEstado(false);
        when(repository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(administrador));
        when(repository.findByNombreIgnoreCase("Médico")).thenReturn(Optional.empty());
        when(repository.findByNombreIgnoreCase("Medico")).thenReturn(Optional.of(medico));

        new PuestosDataInitializer(repository).run();

        assertEquals(true, administrador.isEstado());
        assertEquals("Médico", medico.getNombre());
        assertEquals(true, medico.isEstado());
        verify(repository).save(administrador);
        verify(repository).save(medico);
    }
}