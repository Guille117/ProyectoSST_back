package com.example.demo.modules.usuarios.horarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HorariosDataInitializerTest {

    @Mock
    private HorarioRepository repository;

    @Test
    void run_creaHorarioActivoSinLimitesParaTodosLosDias() {
        when(repository.findByNombreIgnoreCase("Sin limite")).thenReturn(Optional.empty());
        when(repository.findByNombreIgnoreCase("Horario Libre")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);

        new HorariosDataInitializer(repository).run();

        verify(repository).save(argThat(horario ->
                horario.getNombre().equals("Sin limite")
                        && horario.isEstado()
                        && !horario.isEsRotativo()
                        && horario.getSemanalDetalles().size() == DiaSemana.values().length
                        && horario.getSemanalDetalles().stream().allMatch(detalle ->
                                detalle.isActivo()
                                        && detalle.getHoraEntrada().equals(LocalTime.MIN)
                                        && detalle.getHoraSalida().equals(LocalTime.MAX))));
    }

    @Test
    void run_reactivaHorarioExistenteYConservaElRegistro() {
        HorarioEntity existente = HorarioEntity.builder()
                .id(7L).codigo("HOR-07").nombre("Sin limite").estado(false).build();
        when(repository.findByNombreIgnoreCase("Sin limite")).thenReturn(Optional.of(existente));

        new HorariosDataInitializer(repository).run();

        assertEquals("Sin limite", existente.getNombre());
        assertTrue(existente.isEstado());
        assertFalse(existente.isEsRotativo());
        verify(repository).save(existente);
    }
}