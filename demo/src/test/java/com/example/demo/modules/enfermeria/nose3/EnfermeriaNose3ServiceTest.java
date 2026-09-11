package com.example.demo.modules.enfermeria.nose3;

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
class EnfermeriaNose3ServiceTest {

    @Mock
    private EnfermeriaNose3Repository repository;

    @Mock
    private EnfermeriaNose3Mapper mapper;

    @InjectMocks
    private EnfermeriaNose3Service service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        EnfermeriaNose3DTOs.Request req = new EnfermeriaNose3DTOs.Request("Test", true);
        EnfermeriaNose3Entity entity = EnfermeriaNose3Entity.builder().nombre("Test").codigo("ENF3-01").estado(true).build();
        EnfermeriaNose3Entity guardado = EnfermeriaNose3Entity.builder().id(1L).nombre("Test").codigo("ENF3-01").estado(true).build();
        EnfermeriaNose3DTOs.Response resp = new EnfermeriaNose3DTOs.Response(1L, "ENF3-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        EnfermeriaNose3DTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        EnfermeriaNose3DTOs.Request req = new EnfermeriaNose3DTOs.Request("Duplicado", true);
        EnfermeriaNose3Entity dup = EnfermeriaNose3Entity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        EnfermeriaNose3Entity entity = EnfermeriaNose3Entity.builder().id(1L).nombre("Test").codigo("ENF3-01").estado(true).build();
        EnfermeriaNose3DTOs.Response resp = new EnfermeriaNose3DTOs.Response(1L, "ENF3-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        EnfermeriaNose3Entity entity = EnfermeriaNose3Entity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
