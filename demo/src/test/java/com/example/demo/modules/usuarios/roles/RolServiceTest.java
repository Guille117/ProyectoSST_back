package com.example.demo.modules.usuarios.roles;

import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository repository;

    @Mock
    private RolMapper mapper;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private SubmoduloRepository submoduloRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RolService service;

    @Test
    void crear_Exitoso() {
        // Given
        RolDTOs.PermisoRequest permReq = new RolDTOs.PermisoRequest(1L, true, true, false, false);
        RolDTOs.Request req = new RolDTOs.Request("Administrador", true, List.of(permReq));

        RolEntity entity = new RolEntity();
        entity.setNombre("Administrador");

        SubmoduloEntity sub = SubmoduloEntity.builder().id(1L).codigo("ROLES").nombre("Roles").estado(true).build();
        ModuloEntity mod = ModuloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").build();
        sub.setModulo(mod);

        RolEntity guardado = RolEntity.builder()
                .id(1L).codigo("ROL-01").nombre("Administrador").estado(true)
                .build();

        RolDTOs.Response response = new RolDTOs.Response(
                1L, "ROL-01", "Administrador", true,
                List.of(new RolDTOs.PermisoResponse(1L, 1L, "ROLES", "Roles", "USUARIOS", "Usuarios", true, true, false, false))
        );

        when(repository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(submoduloRepository.findById(1L)).thenReturn(Optional.of(sub));
        when(repository.save(any(RolEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        // When
        RolDTOs.Response resultado = service.crear(req);

        // Then
        assertEquals(response, resultado);
        verify(repository).save(any(RolEntity.class));
    }

    @Test
    void crear_LanzaExcepcion_CuandoDatoDuplicado() {
        RolDTOs.Request req = new RolDTOs.Request("Administrador", true, List.of());

        when(repository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(
                RolEntity.builder().id(99L).nombre("Administrador").estado(true).build()
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(req));
        assertEquals("Ya existe un rol activo con el nombre: Administrador", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_LanzaExcepcion_CuandoDuplicadoInactivo() {
        RolDTOs.Request req = new RolDTOs.Request("Administrador", true, List.of());

        when(repository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(
                RolEntity.builder().id(99L).nombre("Administrador").estado(false).build()
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(req));
        assertTrue(ex.getMessage().contains("pero está inactivo"));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_DeduplicaPermisosRepetidosParaMismoSubmodulo() {
        RolDTOs.Request req = new RolDTOs.Request("Administrador", true, List.of(
                new RolDTOs.PermisoRequest(1L, true, false, false, false),
                new RolDTOs.PermisoRequest(1L, false, true, false, false)
        ));

        RolEntity entity = new RolEntity();
        entity.setNombre("Administrador");

        SubmoduloEntity sub = SubmoduloEntity.builder().id(1L).codigo("ROLES").nombre("Roles").estado(true).build();
        ModuloEntity mod = ModuloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").build();
        sub.setModulo(mod);

        RolEntity guardado = RolEntity.builder()
                .id(1L).codigo("ROL-01").nombre("Administrador").estado(true)
                .build();

        when(repository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(submoduloRepository.findById(1L)).thenReturn(Optional.of(sub));
        when(repository.save(any(RolEntity.class))).thenReturn(guardado);

        service.crear(req);

        ArgumentCaptor<RolEntity> captor = ArgumentCaptor.forClass(RolEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(1, captor.getValue().getPermisos().size());
        assertTrue(captor.getValue().getPermisos().getFirst().isPuedeLeer());
        assertTrue(captor.getValue().getPermisos().getFirst().isPuedeCrear());
    }

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
    void actualizar_Exitoso() {
        SubmoduloEntity subExistente = SubmoduloEntity.builder().id(1L).codigo("ROLES").nombre("Roles").estado(true).build();
        subExistente.setModulo(ModuloEntity.builder().codigo("USUARIOS").nombre("Usuarios").build());

        RolEntity existente = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Rol Antiguo").estado(true).build();
        existente.setPermisos(new ArrayList<>(List.of(
                RolPermisoEntity.builder().id(10L).submodulo(subExistente).puedeLeer(true).build()
        )));

        SubmoduloEntity sub = SubmoduloEntity.builder().id(2L).codigo("HORARIOS").nombre("Horarios").estado(true).build();
        sub.setModulo(ModuloEntity.builder().codigo("USUARIOS").nombre("Usuarios").build());

        RolDTOs.PermisoRequest permReq = new RolDTOs.PermisoRequest(2L, true, false, true, false);
        RolDTOs.Request req = new RolDTOs.Request("Rol Actualizado", true, List.of(permReq));

        RolEntity actualizado = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Rol Actualizado").estado(true).build();
        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Rol Actualizado", true, List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByNombreIgnoreCase("Rol Actualizado")).thenReturn(Optional.empty());
        when(submoduloRepository.findById(2L)).thenReturn(Optional.of(sub));
        when(repository.save(any(RolEntity.class))).thenReturn(actualizado);
        when(mapper.toDTO(actualizado)).thenReturn(response);

        RolDTOs.Response resultado = service.actualizar(1L, req);

        assertEquals(response, resultado);
        verify(repository).save(any(RolEntity.class));
        // el submodulo antiguo (1) ya no viene en la petición -> se elimina; solo queda el submodulo 2
        assertEquals(1, existente.getPermisos().size());
        assertEquals(2L, existente.getPermisos().getFirst().getSubmodulo().getId());
    }

    @Test
    void actualizar_LanzaExcepcion_CuandoRolAsociadoCambiaPermisos() {
        SubmoduloEntity submodulo = SubmoduloEntity.builder().id(1L).codigo("HORARIOS").nombre("Horarios").build();
        RolEntity existente = RolEntity.builder().id(1L).nombre("Rol Antiguo").estado(true).build();
        existente.setPermisos(new ArrayList<>(List.of(
                RolPermisoEntity.builder().submodulo(submodulo).puedeLeer(true).puedeCrear(false).puedeEditar(false).puedeEliminar(false).build()
        )));
        RolDTOs.Request request = new RolDTOs.Request(
                "Rol Actualizado", true,
                List.of(new RolDTOs.PermisoRequest(1L, true, true, false, false))
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByNombreIgnoreCase("Rol Actualizado")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByRoles_Id(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, request));

        assertEquals("No se pueden modificar los permisos de un rol asociado a usuarios; solo se permite cambiar el nombre", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void cambiarEstado_Exitoso() {
        RolEntity entity = RolEntity.builder().id(1L).nombre("Administrador").estado(true).build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(usuarioRepository.existsByRoles_Id(1L)).thenReturn(false);
        when(repository.save(any(RolEntity.class))).thenReturn(entity);

        service.cambiarEstado(1L);

        assertFalse(entity.isEstado());
        verify(repository).save(entity);
    }

    @Test
    void cambiarEstado_LanzaExcepcion_CuandoRolEstaAsociadoAUsuario() {
        RolEntity entity = RolEntity.builder().id(1L).nombre("Administrador").estado(true).build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(usuarioRepository.existsByRoles_Id(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(1L));

        assertEquals("No se puede desactivar el rol porque está asociado a uno o más usuarios", exception.getMessage());
        verify(repository, never()).save(any());
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
