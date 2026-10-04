package com.example.demo.modules.farmacia.insumosLog;

import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class insumosLogServiceTest {

    @Mock
    private insumosLogRepository repository;

    @Mock
    private marcaRepository marcaRepository;

    @InjectMocks
    private insumosLogService service;

    @Test
    void obtenerTodos_debeIncluirMarcaAnidadaYEstado() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByEstado(true)).thenReturn(List.of(insumo));

        List<insumosLogDTOs.ListadoResponse> resultado = service.obtenerTodos(true);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Guantes", resultado.get(0).nombre());
        assertEquals("Marca A", resultado.get(0).marca().nombreMarca());
        assertEquals(4L, resultado.get(0).marca().idMarca());
        assertEquals(true, resultado.get(0).estado());
    }

    @Test
    void buscar_debeFiltrarPorNombreYMarca() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByNombreContainingIgnoreCaseAndMarca_IdAndEstado("guan", 4L, true))
                .thenReturn(List.of(insumo));

        List<insumosLogDTOs.ListadoResponse> resultado = service.buscar(" guan ", 4L, true);

        assertEquals(1, resultado.size());
        assertEquals("Marca A", resultado.get(0).marca().nombreMarca());
        assertEquals(4L, resultado.get(0).marca().idMarca());
    }

    @Test
    void cambiarEstado_debeAlternarEstado() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));

        service.cambiarEstado(10L);

        verify(repository).save(insumo);
        assertFalse(insumo.isEstado());
    }

    @Test
    void buscar_sinFiltros_debeRetornarListaVacia() {
        assertEquals(List.of(), service.buscar(null, null, true));
        verify(repository, never()).findByEstado(any(Boolean.class));
    }

    private insumosLogEntity insumo(Long id, String nombre, Long marcaId, String marcaNombre, boolean estado) {
        marcaEntity marca = new marcaEntity();
        marca.setId(marcaId);
        marca.setNombre(marcaNombre);
        return insumosLogEntity.builder()
                .id(id)
                .nombre(nombre)
                .marca(marca)
                .estado(estado)
                .build();
    }
}