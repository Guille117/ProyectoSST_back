package com.example.demo.modules.usuarios.roles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository repository;

    @Mock
    private RolMapper mapper;

    @Mock
    private ModuloRepository moduloRepository;

    @InjectMocks
    private RolService service;

    @Test
    void obtenerPorId_Exitoso() {
        RolEntity entity = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Administrador").estado(true).build();
        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Administrador", true, List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        RolDTOs.Response result = service.obtenerPorId(1L);

        assertEquals(response, result);
    }

    @Test
    void obtenerPorId_LanzaExcepcion_CuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
        assertTrue(ex.getMessage().contains("Rol no encontrado"));
    }

    @Test
    void obtenerTodos_ConFiltroActivos() {
        RolEntity e = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Admin").estado(true).build();
        RolDTOs.Response r = new RolDTOs.Response(1L, "ROL-01", "Admin", true, List.of());

        when(repository.findByEstado(true)).thenReturn(List.of(e));
        when(mapper.toDTO(e)).thenReturn(r);

        List<RolDTOs.Response> res = service.obtenerTodos(true);

        assertEquals(1, res.size());
        assertEquals("Admin", res.get(0).nombre());
    }

    @Test
    void buscarPorCriterio_Exitoso() {
        RolEntity e = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Administrador").estado(true).build();
        RolDTOs.Response r = new RolDTOs.Response(1L, "ROL-01", "Administrador", true, List.of());

        when(repository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase("Admin", "Admin")).thenReturn(List.of(e));
        when(mapper.toDTO(e)).thenReturn(r);

        List<RolDTOs.Response> res = service.buscarPorCriterio("Admin", null);

        assertEquals(1, res.size());
        verify(mapper).toDTO(e);
    }

    @Test
    void buscarPorCriterio_VacioRetornaListaVacia() {
        List<RolDTOs.Response> res = service.buscarPorCriterio("  ", null);
        assertTrue(res.isEmpty());
        verify(repository, never()).findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(any(), any());
    }

    @Test
    void obtenerModulosJerarquicos_Exitoso() {
        ModuloEntity mod = ModuloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").estado(true).build();
        SubmoduloEntity sub = SubmoduloEntity.builder().id(10L).codigo("ROLES").nombre("Roles").estado(true).modulo(mod).build();
        mod.setSubmodulos(List.of(sub));

        RolDTOs.ModuloResponse dto = new RolDTOs.ModuloResponse(1L, "USUARIOS", "Usuarios", true,
                List.of(new RolDTOs.SubmoduloResponse(10L, "ROLES", "Roles", true)));

        when(moduloRepository.findAll()).thenReturn(List.of(mod));
        when(mapper.toModuloDTO(mod)).thenReturn(dto);

        List<RolDTOs.ModuloResponse> res = service.obtenerModulosJerarquicos();

        assertEquals(1, res.size());
        assertEquals("USUARIOS", res.get(0).codigo());
    }
}
