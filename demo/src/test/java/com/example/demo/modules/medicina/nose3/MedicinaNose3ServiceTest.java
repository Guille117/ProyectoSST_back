package com.example.demo.modules.medicina.nose3;

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
class MedicinaNose3ServiceTest {

    @Mock
    private MedicinaNose3Repository repository;

    @Mock
    private MedicinaNose3Mapper mapper;

    @InjectMocks
    private MedicinaNose3Service service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        MedicinaNose3DTOs.Request req = new MedicinaNose3DTOs.Request("Test", true);
        MedicinaNose3Entity entity = MedicinaNose3Entity.builder().nombre("Test").codigo("MED3-01").estado(true).build();
        MedicinaNose3Entity guardado = MedicinaNose3Entity.builder().id(1L).nombre("Test").codigo("MED3-01").estado(true).build();
        MedicinaNose3DTOs.Response resp = new MedicinaNose3DTOs.Response(1L, "MED3-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        MedicinaNose3DTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        MedicinaNose3DTOs.Request req = new MedicinaNose3DTOs.Request("Duplicado", true);
        MedicinaNose3Entity dup = MedicinaNose3Entity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        MedicinaNose3Entity entity = MedicinaNose3Entity.builder().id(1L).nombre("Test").codigo("MED3-01").estado(true).build();
        MedicinaNose3DTOs.Response resp = new MedicinaNose3DTOs.Response(1L, "MED3-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        MedicinaNose3Entity entity = MedicinaNose3Entity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
