package com.example.demo.modules.usuarios.puesto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PuestoServiceTest {

    @Mock
    private puestoRepository repository;

    @InjectMocks
    private puestoService service;

    @Test
    void guardar_rechazaNombreParecidoAUnPuestoDelSistema() {
        puestoEntity puesto = new puestoEntity();
        puesto.setNombre("PuestoMedico");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.guardar(puesto));

        assertEquals("El puesto 'Médico' es un elemento del sistema y no se puede alterar ni duplicar.", error.getMessage());
        verify(repository, never()).save(puesto);
    }

    @Test
    void actualizar_rechazaCambioDePuestoDelSistema() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(1L);
        puesto.setNombre("Administrador");
        when(repository.findById(1L)).thenReturn(Optional.of(puesto));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, "Coordinador"));

        assertEquals("El puesto 'Administrador' es un elemento del sistema y no se puede alterar ni duplicar.", error.getMessage());
        verify(repository, never()).save(puesto);
    }

    @Test
    void cambiarEstado_rechazaDesactivarPuestoDelSistema() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(2L);
        puesto.setNombre("Médico");
        when(repository.findById(2L)).thenReturn(Optional.of(puesto));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(2L));

        assertEquals("El puesto 'Médico' es un elemento del sistema y no se puede alterar ni duplicar.", error.getMessage());
        verify(repository, never()).save(puesto);
    }
}