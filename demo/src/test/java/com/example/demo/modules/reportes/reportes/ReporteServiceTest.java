package com.example.demo.modules.reportes.reportes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository repository;

    @Mock
    private ReporteMapper mapper;

    @InjectMocks
    private ReporteService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        ReporteDTOs.Request req = new ReporteDTOs.Request("Test", true);
        ReporteEntity entity = ReporteEntity.builder().nombre("Test").codigo("REP-01").estado(true).build();
        ReporteEntity guardado = ReporteEntity.builder().id(1L).nombre("Test").codigo("REP-01").estado(true).build();
        ReporteDTOs.Response resp = new ReporteDTOs.Response(1L, "REP-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        ReporteDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        ReporteDTOs.Request req = new ReporteDTOs.Request("Duplicado", true);
        ReporteEntity dup = ReporteEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        ReporteEntity entity = ReporteEntity.builder().id(1L).nombre("Test").codigo("REP-01").estado(true).build();
        ReporteDTOs.Response resp = new ReporteDTOs.Response(1L, "REP-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        ReporteEntity entity = ReporteEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
