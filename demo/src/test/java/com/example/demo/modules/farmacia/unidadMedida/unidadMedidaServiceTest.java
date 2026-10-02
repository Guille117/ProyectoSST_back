package com.example.demo.modules.farmacia.unidadMedida;

import java.util.Optional;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class unidadMedidaServiceTest {

    @Mock
    private unidadMedidaRepository repository;

    @Mock
    private medicamentoLogRepository medicamentoLogRepository;

    @InjectMocks
    private unidadMedidaService service;

    @Test
    void actualizar_debeGuardarNombreYAabreviatura() {
        unidadMedidaEntity actual = new unidadMedidaEntity();
        actual.setId(6L);
        actual.setNombre("Gramo");
        actual.setAbreviatura("g");

        unidadMedidaEntity detalles = new unidadMedidaEntity();
        detalles.setNombre("Kilogramo");
        detalles.setAbreviatura("kg");

        when(repository.findById(6L)).thenReturn(Optional.of(actual));
        when(repository.findByNombreIgnoreCase("Kilogramo")).thenReturn(Optional.empty());
        when(repository.save(actual)).thenReturn(actual);

        Optional<unidadMedidaEntity> resultado = service.actualizar(6L, detalles);

        assertEquals("Kilogramo", resultado.orElseThrow().getNombre());
        assertEquals("kg", resultado.orElseThrow().getAbreviatura());
        verify(repository).save(actual);
    }

    @Test
    void actualizar_debeDevolverVacioCuandoNoExisteLaUnidad() {
        unidadMedidaEntity detalles = new unidadMedidaEntity();
        detalles.setNombre("Kilogramo");
        detalles.setAbreviatura("kg");
        when(repository.findById(6L)).thenReturn(Optional.empty());

        Optional<unidadMedidaEntity> resultado = service.actualizar(6L, detalles);

        assertFalse(resultado.isPresent());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeRechazarNombreVacio() {
        unidadMedidaEntity detalles = new unidadMedidaEntity();
        detalles.setNombre(" ");
        detalles.setAbreviatura("kg");

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(6L, detalles));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeRechazarUnidadRelacionadaConMedicamentos() {
        unidadMedidaEntity actual = new unidadMedidaEntity();
        actual.setId(6L);
        actual.setNombre("Gramo");
        unidadMedidaEntity detalles = new unidadMedidaEntity();
        detalles.setNombre("Kilogramo");
        detalles.setAbreviatura("kg");
        when(repository.findById(6L)).thenReturn(Optional.of(actual));
        when(medicamentoLogRepository.existsByUnidadMedida_Id(6L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(6L, detalles));
        verify(repository, never()).save(any());
    }

    @Test
    void cambiarEstado_debeRechazarDesactivarUnidadRelacionadaConMedicamentos() {
        unidadMedidaEntity actual = new unidadMedidaEntity();
        actual.setId(6L);
        actual.setNombre("Gramo");
        actual.setEstado(true);
        when(repository.findById(6L)).thenReturn(Optional.of(actual));
        when(medicamentoLogRepository.existsByUnidadMedida_Id(6L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(6L));
        verify(repository, never()).save(any());
    }
}