package com.example.demo.modules.reportes.bitacora;

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
class BitacoraServiceTest {

    @Mock
    private BitacoraRepository repository;

    @Mock
    private BitacoraMapper mapper;

    @InjectMocks
    private BitacoraService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        BitacoraDTOs.Request req = new BitacoraDTOs.Request("Test", true);
        BitacoraEntity entity = BitacoraEntity.builder().nombre("Test").codigo("BIT-01").estado(true).build();
        BitacoraEntity guardado = BitacoraEntity.builder().id(1L).nombre("Test").codigo("BIT-01").estado(true).build();
        BitacoraDTOs.Response resp = new BitacoraDTOs.Response(1L, "BIT-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        BitacoraDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        BitacoraDTOs.Request req = new BitacoraDTOs.Request("Duplicado", true);
        BitacoraEntity dup = BitacoraEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        BitacoraEntity entity = BitacoraEntity.builder().id(1L).nombre("Test").codigo("BIT-01").estado(true).build();
        BitacoraDTOs.Response resp = new BitacoraDTOs.Response(1L, "BIT-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        BitacoraEntity entity = BitacoraEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
