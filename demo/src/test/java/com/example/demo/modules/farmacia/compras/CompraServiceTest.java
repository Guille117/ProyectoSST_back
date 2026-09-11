package com.example.demo.modules.farmacia.compras;

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
class CompraServiceTest {

    @Mock
    private CompraRepository repository;

    @Mock
    private CompraMapper mapper;

    @InjectMocks
    private CompraService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        CompraDTOs.Request req = new CompraDTOs.Request("Test", true);
        CompraEntity entity = CompraEntity.builder().nombre("Test").codigo("COMP-01").estado(true).build();
        CompraEntity guardado = CompraEntity.builder().id(1L).nombre("Test").codigo("COMP-01").estado(true).build();
        CompraDTOs.Response resp = new CompraDTOs.Response(1L, "COMP-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        CompraDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        CompraDTOs.Request req = new CompraDTOs.Request("Duplicado", true);
        CompraEntity dup = CompraEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        CompraEntity entity = CompraEntity.builder().id(1L).nombre("Test").codigo("COMP-01").estado(true).build();
        CompraDTOs.Response resp = new CompraDTOs.Response(1L, "COMP-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        CompraEntity entity = CompraEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
