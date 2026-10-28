package com.example.demo.modules.pacientes.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpedienteCodigoInitializerTest {

    @Mock
    private ExpedienteRepository repository;

    @Test
    @SuppressWarnings("unchecked")
    void run_asignaCodigoSoloCuandoFalta() {
        ExpedienteEntity sinCodigo = ExpedienteEntity.builder().id(1L).codigo(null).build();
        ExpedienteEntity personalizado = ExpedienteEntity.builder().id(2L).codigo("CUSTOM-2").build();
        ExpedienteEntity vacio = ExpedienteEntity.builder().id(3L).codigo("  ").build();
        when(repository.findAll()).thenReturn(List.of(sinCodigo, personalizado, vacio));

        new ExpedienteCodigoInitializer(repository).run();

        assertEquals("EXP-1", sinCodigo.getCodigo());
        assertEquals("CUSTOM-2", personalizado.getCodigo());
        assertEquals("EXP-3", vacio.getCodigo());

        ArgumentCaptor<List<ExpedienteEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(List.of(sinCodigo, vacio), captor.getValue());
    }
}
