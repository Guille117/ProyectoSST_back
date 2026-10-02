package com.example.demo.modules.pacientes.institucion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class institucionServiceTest {

    @Mock
    private institucionRepository repository;

    @InjectMocks
    private institucionService service;

    @Test
    void guardar_permiteDireccionYTelefonoNulos() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre(" Hospital Central ");
        when(repository.findByNombreIgnoreCase("Hospital Central")).thenReturn(Optional.empty());
        when(repository.save(any(institucionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        institucionEntity guardada = service.guardar(institucion);

        assertEquals("Hospital Central", guardada.getNombre());
        assertNull(guardada.getDireccion());
        assertNull(guardada.getTelefono());
        verify(repository).save(institucion);
    }

    @Test
    void guardar_rechazaNombreVacio() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre("  ");

        assertThrows(IllegalArgumentException.class, () -> service.guardar(institucion));

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorNombre_buscaSinDistinguirMayusculas() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre("Hospital Central");
        when(repository.findAll()).thenReturn(List.of(institucion));

        assertEquals(List.of(institucion), service.buscarPorNombre(" CENTRAL "));
    }

    @Test
    void contarActivas_cuentaSoloInstitucionesActivas() {
        when(repository.countByEstado(true)).thenReturn(4L);

        assertEquals(4L, service.contarActivas());
    }
}