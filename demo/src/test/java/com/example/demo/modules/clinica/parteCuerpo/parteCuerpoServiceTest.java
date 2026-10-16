package com.example.demo.modules.clinica.parteCuerpo;

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
class parteCuerpoServiceTest {

    @Mock
    private parteCuerpoRepository repository;

    @InjectMocks
    private parteCuerpoService service;

    @Test
    void contarPartesCuerpo_debeContarSoloLasActivas() {
        when(repository.countByEstado(true)).thenReturn(7L);

        assertEquals(7L, service.contarPartesCuerpo());
        verify(repository).countByEstado(true);
    }

    @Test
    void guardar_debeNormalizarYGuardarCuandoNoHayDuplicado() {
        parteCuerpoEntity nueva = new parteCuerpoEntity();
        nueva.setNombre("  Abdomen  ");
        when(repository.findByNombreIgnoreCase("Abdomen")).thenReturn(Optional.empty());
        when(repository.save(any(parteCuerpoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        parteCuerpoEntity guardada = service.guardar(nueva);

        assertEquals("Abdomen", guardada.getNombre());
        assertTrue(guardada.isEstado());
    }

    @Test
    void guardar_debeRechazarNombreDuplicadoActivo() {
        parteCuerpoEntity existente = new parteCuerpoEntity();
        existente.setId(1L);
        existente.setNombre("Abdomen");
        existente.setEstado(true);
        parteCuerpoEntity nueva = new parteCuerpoEntity();
        nueva.setNombre(" abdomen ");
        when(repository.findByNombreIgnoreCase("abdomen")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.guardar(nueva));
        verify(repository, never()).save(nueva);
    }
}
