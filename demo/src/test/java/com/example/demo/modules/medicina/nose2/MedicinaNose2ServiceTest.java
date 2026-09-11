package com.example.demo.modules.medicina.nose2;

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
class MedicinaNose2ServiceTest {

    @Mock
    private MedicinaNose2Repository repository;

    @Mock
    private MedicinaNose2Mapper mapper;

    @InjectMocks
    private MedicinaNose2Service service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        MedicinaNose2DTOs.Request req = new MedicinaNose2DTOs.Request("Test", true);
        MedicinaNose2Entity entity = MedicinaNose2Entity.builder().nombre("Test").codigo("MED2-01").estado(true).build();
        MedicinaNose2Entity guardado = MedicinaNose2Entity.builder().id(1L).nombre("Test").codigo("MED2-01").estado(true).build();
        MedicinaNose2DTOs.Response resp = new MedicinaNose2DTOs.Response(1L, "MED2-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        MedicinaNose2DTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        MedicinaNose2DTOs.Request req = new MedicinaNose2DTOs.Request("Duplicado", true);
        MedicinaNose2Entity dup = MedicinaNose2Entity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        MedicinaNose2Entity entity = MedicinaNose2Entity.builder().id(1L).nombre("Test").codigo("MED2-01").estado(true).build();
        MedicinaNose2DTOs.Response resp = new MedicinaNose2DTOs.Response(1L, "MED2-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        MedicinaNose2Entity entity = MedicinaNose2Entity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
