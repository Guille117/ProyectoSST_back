package com.example.demo.modules.usuarios.horarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository repository;

    @Mock
    private HorarioMapper mapper;

    @InjectMocks
    private HorarioService service;

    @Test
    void crear_Exitoso() {
        // Given - horario no rotativo con detalles semanales
        HorarioDTOs.DetalleSemanalRequest detalleReq = new HorarioDTOs.DetalleSemanalRequest(
                DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true
        );
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Diurno",
                false,
                true,
                List.of(detalleReq),
                null
        );

        HorarioEntity entity = new HorarioEntity();
        entity.setNombre("Horario Diurno");

        HorarioEntity guardado = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Horario Diurno")
                .esRotativo(false)
                .estado(true)
                .build();

        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Horario Diurno", false, true,
                List.of(new HorarioDTOs.DetalleSemanalResponse(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true)),
                null
        );

        when(repository.findByNombreIgnoreCase("Horario Diurno")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any(HorarioEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        // When
        HorarioDTOs.Response resultado = service.crear(req);

        // Then
        assertEquals(response, resultado);
        verify(repository).save(any(HorarioEntity.class));
    }

    @Test
    void crear_Exitoso_CuandoDetalleInactivoTieneHorasIguales() {
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Regular",
                false,
                true,
                List.of(
                        new HorarioDTOs.DetalleSemanalRequest(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true),
                        new HorarioDTOs.DetalleSemanalRequest(DiaSemana.DOMINGO, LocalTime.of(0, 0), LocalTime.of(0, 0), false)
                ),
                null
        );

        HorarioEntity entity = new HorarioEntity();
        HorarioEntity guardado = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Horario Regular")
                .esRotativo(false)
                .estado(true)
                .build();
        HorarioDTOs.Response response = new HorarioDTOs.Response(1L, "HOR-01", "Horario Regular", false, true, List.of(), null);

        when(repository.findByNombreIgnoreCase("Horario Regular")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any(HorarioEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        HorarioDTOs.Response resultado = service.crear(req);

        assertEquals(response, resultado);
        verify(repository).save(argThat(h -> h.getSemanalDetalles().stream()
                .anyMatch(d -> d.getDiaSemana() == DiaSemana.DOMINGO && !d.isActivo())));
    }

    @Test
    void crear_Exitoso_Rotativo() {
        // Given - horario rotativo con horas trabajo/descanso
        HorarioDTOs.TurnoDetalleRequest turnoReq = new HorarioDTOs.TurnoDetalleRequest(12, 24);
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Turno Rotativo",
                true,
                true,
                null,
                turnoReq
        );

        HorarioEntity entity = new HorarioEntity();
        entity.setNombre("Turno Rotativo");

        HorarioEntity guardado = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Turno Rotativo")
                .esRotativo(true)
                .estado(true)
                .build();

        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Turno Rotativo", true, true,
                List.of(),
                new HorarioDTOs.TurnoDetalleResponse(1L, 12, 24)
        );

        when(repository.findByNombreIgnoreCase("Turno Rotativo")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any(HorarioEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        // When
        HorarioDTOs.Response resultado = service.crear(req);

        // Then
        assertEquals(response, resultado);
        verify(repository).save(argThat(h -> h.isEsRotativo() && h.getTurnoDetalle() != null));
    }

    @Test
    void crear_LanzaExcepcion_CuandoDatoDuplicado() {
        // Given
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Diurno",
                false,
                true,
                List.of(new HorarioDTOs.DetalleSemanalRequest(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true)),
                null
        );

        when(repository.findByNombreIgnoreCase("Horario Diurno")).thenReturn(Optional.of(
                HorarioEntity.builder().id(99L).nombre("Horario Diurno").estado(true).build()
        ));

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(req)
        );

        assertEquals("Ya existe un horario activo con el nombre: Horario Diurno", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_LanzaExcepcion_CuandoEsRotativoFalseSinDetalles() {
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Sin Detalles",
                false,
                true,
                List.of(),
                null
        );

        when(repository.findByNombreIgnoreCase("Horario Sin Detalles")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(req));
        assertEquals("Debe proporcionar al menos un detalle semanal cuando es_rotativo es false", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_LanzaExcepcion_CuandoEsRotativoTrueSinTurno() {
        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Rotativo Incompleto",
                true,
                true,
                null,
                null
        );

        when(repository.findByNombreIgnoreCase("Horario Rotativo Incompleto")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(req));
        assertEquals("Debe proporcionar el detalle de turno cuando es_rotativo es true", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void obtenerPorId_Exitoso() {
        HorarioEntity entity = HorarioEntity.builder().id(1L).codigo("HOR-01").nombre("Horario Diurno").estado(true).build();
        HorarioDTOs.Response response = new HorarioDTOs.Response(1L, "HOR-01", "Horario Diurno", false, true, List.of(), null);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        HorarioDTOs.Response result = service.obtenerPorId(1L);

        assertEquals(response, result);
    }

    @Test
    void obtenerPorId_LanzaExcepcion_CuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
        assertTrue(ex.getMessage().contains("Horario no encontrado"));
    }

    @Test
    void actualizar_Exitoso() {
        // Given
        HorarioEntity existente = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Horario Antiguo")
                .esRotativo(false)
                .estado(true)
                .build();
        existente.setSemanalDetalles(new java.util.ArrayList<>());

        HorarioDTOs.Request req = new HorarioDTOs.Request(
                "Horario Actualizado",
                false,
                true,
                List.of(new HorarioDTOs.DetalleSemanalRequest(DiaSemana.MARTES, LocalTime.of(9, 0), LocalTime.of(18, 0), true)),
                null
        );

        HorarioEntity actualizado = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Horario Actualizado")
                .esRotativo(false)
                .estado(true)
                .build();

        HorarioDTOs.Response response = new HorarioDTOs.Response(1L, "HOR-01", "Horario Actualizado", false, true, List.of(), null);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByNombreIgnoreCase("Horario Actualizado")).thenReturn(Optional.empty());
        when(repository.save(any(HorarioEntity.class))).thenReturn(actualizado);
        when(mapper.toDTO(actualizado)).thenReturn(response);

        // When
        HorarioDTOs.Response resultado = service.actualizar(1L, req);

        // Then
        assertEquals(response, resultado);
        verify(repository).save(any(HorarioEntity.class));
    }

    @Test
    void cambiarEstado_Exitoso() {
        HorarioEntity entity = HorarioEntity.builder().id(1L).nombre("Horario Diurno").estado(true).build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(HorarioEntity.class))).thenReturn(entity);

        service.cambiarEstado(1L);

        assertFalse(entity.isEstado());
        verify(repository).save(entity);
    }
}
