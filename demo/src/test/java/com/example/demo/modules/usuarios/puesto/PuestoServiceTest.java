package com.example.demo.modules.usuarios.puesto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PuestoServiceTest {

    @Mock
    private puestoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private puestoService service;

    @Test
    void guardar_permiteCrearPuestoDelSistemaCuandoNoExiste() {
        puestoEntity puesto = new puestoEntity();
        puesto.setNombre("Medico");
        when(repository.findByNombreIgnoreCase("Medico")).thenReturn(Optional.empty());
        when(repository.save(puesto)).thenReturn(puesto);

        puestoEntity guardado = service.guardar(puesto);

        assertEquals("Medico", guardado.getNombre());
        verify(repository).save(puesto);
    }

    @Test
    void guardar_rechazaDuplicarUnPuestoExistente() {
        puestoEntity existente = new puestoEntity();
        existente.setId(1L);
        existente.setNombre("Medico");
        existente.setEstado(true);
        puestoEntity nuevo = new puestoEntity();
        nuevo.setNombre("Medico");

        when(repository.findByNombreIgnoreCase("Medico")).thenReturn(Optional.of(existente));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> service.guardar(nuevo));

        assertEquals("Ya existe un registro activo con el nombre: Medico", error.getMessage());
        verify(repository, never()).save(nuevo);
    }

    @Test
    void guardar_permitePuestoCompuestoConNombreDeRol() {
        puestoEntity puesto = new puestoEntity();
        puesto.setNombre("Médico General");
        when(repository.findByNombreIgnoreCase("Médico General")).thenReturn(Optional.empty());
        when(repository.save(puesto)).thenReturn(puesto);

        puestoEntity guardado = service.guardar(puesto);

        assertEquals("Médico General", guardado.getNombre());
        verify(repository).save(puesto);
    }

    @Test
    void actualizar_rechazaCambioDePuestoDelSistema() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(1L);
        puesto.setNombre("Administrador");
        when(repository.findById(1L)).thenReturn(Optional.of(puesto));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, "Coordinador"));

        assertEquals("El puesto 'Administrador' es un elemento del sistema y no se puede alterar ni duplicar.", error.getMessage());
        verify(repository, never()).save(puesto);
    }

    @Test
    void cambiarEstado_rechazaDesactivarPuestoDelSistema() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(2L);
        puesto.setNombre("Médico");
        when(repository.findById(2L)).thenReturn(Optional.of(puesto));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(2L));

        assertEquals("El puesto 'Médico' es un elemento del sistema y no se puede alterar ni duplicar.", error.getMessage());
        verify(repository, never()).save(puesto);
    }

    @Test
    void actualizar_rechazaPuestoAsignadoAUsuario() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(3L);
        puesto.setNombre("Recepcionista");
        when(repository.findById(3L)).thenReturn(Optional.of(puesto));
        when(usuarioRepository.existsByPuesto_Id(3L)).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(3L, "Coordinador"));

        assertEquals("No se puede editar el registro porque está relacionado con otro registro.", error.getMessage());
        verify(repository, never()).save(puesto);
    }

    @Test
    void cambiarEstado_rechazaDesactivarPuestoAsignadoAUsuario() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(3L);
        puesto.setNombre("Recepcionista");
        puesto.setEstado(true);
        when(repository.findById(3L)).thenReturn(Optional.of(puesto));
        when(usuarioRepository.existsByPuesto_Id(3L)).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(3L));

        assertEquals("No se puede desactivar el registro porque está relacionado con otro registro.", error.getMessage());
        verify(repository, never()).save(puesto);
    }
}