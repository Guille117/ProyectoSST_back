package com.example.demo.modules.farmacia.salidas;

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
class SalidaServiceTest {

    @Mock
    private SalidaRepository repository;

    @Mock
    private SalidaMapper mapper;

    @InjectMocks
    private SalidaService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        SalidaDTOs.Request req = new SalidaDTOs.Request("Test", true);
        SalidaEntity entity = SalidaEntity.builder().nombre("Test").codigo("SAL-01").estado(true).build();
        SalidaEntity guardado = SalidaEntity.builder().id(1L).nombre("Test").codigo("SAL-01").estado(true).build();
        SalidaDTOs.Response resp = new SalidaDTOs.Response(1L, "SAL-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        SalidaDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        SalidaDTOs.Request req = new SalidaDTOs.Request("Duplicado", true);
        SalidaEntity dup = SalidaEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        SalidaEntity entity = SalidaEntity.builder().id(1L).nombre("Test").codigo("SAL-01").estado(true).build();
        SalidaDTOs.Response resp = new SalidaDTOs.Response(1L, "SAL-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        SalidaEntity entity = SalidaEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
