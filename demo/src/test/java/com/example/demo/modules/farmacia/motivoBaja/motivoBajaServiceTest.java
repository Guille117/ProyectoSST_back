package com.example.demo.modules.farmacia.motivoBaja;

import java.util.Optional;
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
class motivoBajaServiceTest {

    @Mock
    private motivoBajaRepository repository;

    @InjectMocks
    private motivoBajaService service;

    @Test
    void actualizar_debeGuardarNombreYDescripcion() {
        motivoBajaEntity actual = new motivoBajaEntity();
        actual.setId(6L);
        actual.setNombre("Vencido");
        actual.setDescripcion("Producto vencido");

        motivoBajaEntity detalles = new motivoBajaEntity();
        detalles.setNombre("Dañado");
        detalles.setDescripcion("Producto dañado por mal almacenamiento");

        when(repository.findById(6L)).thenReturn(Optional.of(actual));
        when(repository.findByNombreIgnoreCase("Dañado")).thenReturn(Optional.empty());
        when(repository.save(actual)).thenReturn(actual);

        Optional<motivoBajaEntity> resultado = service.actualizar(6L, detalles);

        assertEquals("Dañado", resultado.orElseThrow().getNombre());
        assertEquals("Producto dañado por mal almacenamiento", resultado.orElseThrow().getDescripcion());
        verify(repository).save(actual);
    }

    @Test
    void actualizar_debeDevolverVacioCuandoNoExisteElMotivo() {
        motivoBajaEntity detalles = new motivoBajaEntity();
        detalles.setNombre("Dañado");
        detalles.setDescripcion("Producto dañado");
        when(repository.findById(6L)).thenReturn(Optional.empty());

        Optional<motivoBajaEntity> resultado = service.actualizar(6L, detalles);

        assertFalse(resultado.isPresent());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeRechazarNombreVacio() {
        motivoBajaEntity detalles = new motivoBajaEntity();
        detalles.setNombre(" ");
        detalles.setDescripcion("Producto dañado");

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(6L, detalles));
        verify(repository, never()).save(any());
    }
}
