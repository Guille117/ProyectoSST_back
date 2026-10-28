package com.example.demo.modules.pacientes.institucion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.modules.pacientes.paciente.ReferenciaRepository;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class institucionServiceTest {

    @Mock
    private institucionRepository repository;

    @Mock
    private ReferenciaRepository referenciaRepository;

    @InjectMocks
    private institucionService service;

    @Test
    void guardar_permiteDireccionYTelefonoNulos() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre(" Hospital Central ");
        when(repository.findByNombreIgnoreCase("Hospital Central")).thenReturn(Optional.empty());
        when(repository.save(any(institucionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        institucionEntity guardada = service.guardar(institucion);

        assertEquals("Hospital Central", guardada.getNombre());
        assertNull(guardada.getDireccion());
        assertNull(guardada.getTelefono());
        verify(repository).save(institucion);
    }

    @Test
    void guardar_rechazaNombreVacio() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre("  ");

        assertThrows(IllegalArgumentException.class, () -> service.guardar(institucion));

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorNombre_buscaSinDistinguirMayusculas() {
        institucionEntity institucion = new institucionEntity();
        institucion.setNombre("Hospital Central");
        when(repository.findAll()).thenReturn(List.of(institucion));

        assertEquals(List.of(institucion), service.buscarPorNombre(" CENTRAL "));
    }

    @Test
    void contarActivas_cuentaSoloInstitucionesActivas() {
        when(repository.countByEstado(true)).thenReturn(4L);

        assertEquals(4L, service.contarActivas());
    }

    @Test
    void cambiarEstado_rechazaDesactivarInstitucionUsadaEnReferencia() {
        institucionEntity institucion = new institucionEntity();
        institucion.setId(8L);
        institucion.setNombre("Centro de salud");
        institucion.setEstado(true);
        when(repository.findById(8L)).thenReturn(Optional.of(institucion));
        when(referenciaRepository.existsByInstitucion_Id(8L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(8L));
        verify(repository, never()).save(institucion);
    }
}