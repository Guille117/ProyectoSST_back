package com.example.demo.modules.clinica.condicionEgreso;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class condicionEgresoServiceTest {

    @Mock
    private condicionEgresoRepository repository;

    @InjectMocks
    private condicionEgresoService service;

    @Test
    void contarCondicionesEgreso_debeContarSoloLasActivas() {
        when(repository.countByEstado(true)).thenReturn(4L);

        assertEquals(4L, service.contarCondicionesEgreso());
        verify(repository).countByEstado(true);
    }

    @Test
    void guardar_debeNormalizarYGuardarCuandoNoHayDuplicado() {
        condicionEgresoEntity nueva = new condicionEgresoEntity();
        nueva.setNombre("  Curado  ");
        when(repository.findByNombreIgnoreCase("Curado")).thenReturn(Optional.empty());
        when(repository.save(any(condicionEgresoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        condicionEgresoEntity guardada = service.guardar(nueva);

        assertEquals("Curado", guardada.getNombre());
        assertTrue(guardada.isEstado());
    }

    @Test
    void guardar_debeRechazarNombreDuplicadoActivo() {
        condicionEgresoEntity existente = new condicionEgresoEntity();
        existente.setId(1L);
        existente.setNombre("Curado");
        existente.setEstado(true);
        condicionEgresoEntity nueva = new condicionEgresoEntity();
        nueva.setNombre(" curado ");
        when(repository.findByNombreIgnoreCase("curado")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.guardar(nueva));
        verify(repository, never()).save(nueva);
    }
}
