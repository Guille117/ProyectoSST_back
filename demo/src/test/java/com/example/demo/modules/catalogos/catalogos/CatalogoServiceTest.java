package com.example.demo.modules.catalogos.catalogos;

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
class CatalogoServiceTest {

    @Mock
    private CatalogoRepository repository;

    @Mock
    private CatalogoMapper mapper;

    @InjectMocks
    private CatalogoService service;

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        CatalogoDTOs.Request req = new CatalogoDTOs.Request("Test", true);
        CatalogoEntity entity = CatalogoEntity.builder().nombre("Test").codigo("CAT-01").estado(true).build();
        CatalogoEntity guardado = CatalogoEntity.builder().id(1L).nombre("Test").codigo("CAT-01").estado(true).build();
        CatalogoDTOs.Response resp = new CatalogoDTOs.Response(1L, "CAT-01", "Test", true);

        when(repository.findByNombreIgnoreCase("Test")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        CatalogoDTOs.Response result = service.crear(req);

        assertThat(result.nombre()).isEqualTo("Test");
        verify(repository).save(any());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoDuplicado() {
        CatalogoDTOs.Request req = new CatalogoDTOs.Request("Duplicado", true);
        CatalogoEntity dup = CatalogoEntity.builder().id(1L).nombre("Duplicado").estado(true).build();
        when(repository.findByNombreIgnoreCase("Duplicado")).thenReturn(Optional.of(dup));

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        CatalogoEntity entity = CatalogoEntity.builder().id(1L).nombre("Test").codigo("CAT-01").estado(true).build();
        CatalogoDTOs.Response resp = new CatalogoDTOs.Response(1L, "CAT-01", "Test", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        CatalogoEntity entity = CatalogoEntity.builder().id(1L).nombre("Test").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }
}
