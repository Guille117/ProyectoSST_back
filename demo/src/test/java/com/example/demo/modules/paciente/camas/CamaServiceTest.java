package com.example.demo.modules.paciente.camas;

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
class CamaServiceTest {

    @Mock
    private CamaRepository repository;

    @Mock
    private CamaMapper mapper;

    @InjectMocks
    private CamaService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        CamaDTOs.Request req = new CamaDTOs.Request("Test", true);
        CamaEntity entity = CamaEntity.builder().nombre("Test").codigo("CAMA-01").estado(true).build();
        CamaEntity guardado = CamaEntity.builder().id(1L).nombre("Test").codigo("CAMA-01").estado(true).build();
        CamaDTOs.Response resp = new CamaDTOs.Response(1L, "CAMA-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        CamaDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        CamaDTOs.Request req = new CamaDTOs.Request("Duplicado", true);
        CamaEntity dup = CamaEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        CamaEntity entity = CamaEntity.builder().id(1L).nombre("Test").codigo("CAMA-01").estado(true).build();
        CamaDTOs.Response resp = new CamaDTOs.Response(1L, "CAMA-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        CamaEntity entity = CamaEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
