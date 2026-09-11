package com.example.demo.modules.enfermeria.nose1;

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
class EnfermeriaNose1ServiceTest {

    @Mock
    private EnfermeriaNose1Repository repository;

    @Mock
    private EnfermeriaNose1Mapper mapper;

    @InjectMocks
    private EnfermeriaNose1Service service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        EnfermeriaNose1DTOs.Request req = new EnfermeriaNose1DTOs.Request("Test", true);
        EnfermeriaNose1Entity entity = EnfermeriaNose1Entity.builder().nombre("Test").codigo("ENF1-01").estado(true).build();
        EnfermeriaNose1Entity guardado = EnfermeriaNose1Entity.builder().id(1L).nombre("Test").codigo("ENF1-01").estado(true).build();
        EnfermeriaNose1DTOs.Response resp = new EnfermeriaNose1DTOs.Response(1L, "ENF1-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        EnfermeriaNose1DTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        EnfermeriaNose1DTOs.Request req = new EnfermeriaNose1DTOs.Request("Duplicado", true);
        EnfermeriaNose1Entity dup = EnfermeriaNose1Entity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        EnfermeriaNose1Entity entity = EnfermeriaNose1Entity.builder().id(1L).nombre("Test").codigo("ENF1-01").estado(true).build();
        EnfermeriaNose1DTOs.Response resp = new EnfermeriaNose1DTOs.Response(1L, "ENF1-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        EnfermeriaNose1Entity entity = EnfermeriaNose1Entity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
