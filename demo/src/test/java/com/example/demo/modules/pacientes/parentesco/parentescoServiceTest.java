package com.example.demo.modules.pacientes.parentesco;

import java.util.List;
import java.util.Optional;
import com.example.demo.modules.pacientes.paciente.PersonaResponsableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class parentescoServiceTest {

    @Mock
    private parentescoRepository repository;

    @Mock
    private parentescoMapper mapper;

    @Mock
    private PersonaResponsableRepository responsableRepository;

    @InjectMocks
    private parentescoService service;

    @Test
    void crear_debeGuardarNombreNormalizado() {
        parentescoDTOs.Request request = new parentescoDTOs.Request("  Madre  ", null);
        parentescoEntity entity = new parentescoEntity();
        parentescoEntity guardado = crearEntidad(1L, "Madre");
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Madre", true);

        when(repository.findByNombreIgnoreCase("Madre")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        parentescoDTOs.Response resultado = service.crear(request);

        assertEquals(response, resultado);
        assertEquals("Madre", entity.getNombre());
        org.junit.jupiter.api.Assertions.assertTrue(entity.isEstado());
        verify(repository).save(entity);
    }

    @Test
    void crear_debeRechazarNombreDuplicado() {
        parentescoDTOs.Request request = new parentescoDTOs.Request("Madre", null);
        when(mapper.toEntity(request)).thenReturn(new parentescoEntity());
        when(repository.findByNombreIgnoreCase("Madre"))
            .thenReturn(Optional.of(crearEntidad(1L, "Madre")));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

    @Test
    void obtenerPorId_debeRetornarParentescoExistente() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Madre", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        assertEquals(response, service.obtenerPorId(1L));
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void actualizar_debeModificarNombre() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        parentescoDTOs.Request request = new parentescoDTOs.Request("Tutor", null);
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Tutor", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.findByNombreIgnoreCase("Tutor")).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(response);

        assertEquals(response, service.actualizar(1L, request));
        assertEquals("Tutor", entity.getNombre());
    }

    @Test
    void obtenerTodos_debeMapearRegistros() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Madre", true);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        assertEquals(List.of(response), service.obtenerTodos(null));
    }

    @Test
    void obtenerTodos_debeFiltrarPorEstado() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Madre", true);
        when(repository.findByEstado(true)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        assertEquals(List.of(response), service.obtenerTodos(true));
    }

    @Test
    void contarActivos_debeContarSoloRegistrosActivos() {
        when(repository.countByEstado(true)).thenReturn(3L);

        assertEquals(3L, service.contarActivos());
    }

    @Test
    void cambiarEstado_debeAlternarEstado() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        entity.setEstado(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(new parentescoDTOs.Response(1L, "Madre", false));

        assertEquals(false, service.alternarEstado(1L).orElseThrow().estado());
        verify(repository).save(entity);
    }

    @Test
    void cambiarEstado_debeRechazarParentescoRelacionadoConResponsable() {
        parentescoEntity entity = crearEntidad(1L, "Madre");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(responsableRepository.existsByParentesco_Id(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.alternarEstado(1L));
        verify(repository, never()).save(entity);
    }

    private parentescoEntity crearEntidad(Long id, String nombre) {
        parentescoEntity entity = new parentescoEntity();
        entity.setId(id);
        entity.setNombre(nombre);
        entity.setEstado(true);
        return entity;
    }
}