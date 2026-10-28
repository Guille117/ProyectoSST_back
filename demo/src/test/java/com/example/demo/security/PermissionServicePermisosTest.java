package com.example.demo.security;

import com.example.demo.modules.usuarios.roles.ModuloEntity;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolPermisoEntity;
import com.example.demo.modules.usuarios.roles.SubmoduloEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServicePermisosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @Test
    void puedeModulo_usaCualquierSubmoduloDelModulo() {
        ModuloEntity modulo = modulo("MEDICA");
        SubmoduloEntity atencion = submodulo(1L, modulo, "MED-ATENCION");
        RolPermisoEntity permiso = permiso(atencion, true, true, false, false);
        PermissionService service = serviceCon("medico", permiso);

        assertTrue(service.puedeModulo(authentication, "MEDICA", AccionPermiso.LEER));
        assertTrue(service.puedeModulo(authentication, "MEDICA", AccionPermiso.CREAR));
        assertFalse(service.puedeModulo(authentication, "MEDICA", AccionPermiso.ELIMINAR));
    }

    @Test
    void puedeModulo_soloLecturaNoPermiteEscritura() {
        ModuloEntity modulo = modulo("ENFERMERIA");
        SubmoduloEntity monitoreo = submodulo(2L, modulo, "ENF-MONITOREO");
        RolPermisoEntity permiso = permiso(monitoreo, true, false, false, false);
        PermissionService service = serviceCon("director", permiso);

        assertTrue(service.puedeModulo(authentication, "ENFERMERIA", AccionPermiso.LEER));
        assertFalse(service.puedeModulo(authentication, "ENFERMERIA", AccionPermiso.CREAR));
        assertFalse(service.puedeModulo(authentication, "ENFERMERIA", AccionPermiso.EDITAR));
        assertFalse(service.puedeModulo(authentication, "ENFERMERIA", AccionPermiso.ELIMINAR));
    }

    @Test
    void puedeSubmodulo_esExactoYNoConcedeOtroCatalogoDelMismoModulo() {
        ModuloEntity catalogos = modulo("CATALOGOS");
        SubmoduloEntity clinica = submodulo(3L, catalogos, "CATALOGOS-CLINICA");
        RolPermisoEntity permiso = permiso(clinica, true, true, true, true);
        PermissionService service = serviceCon("enfermera", permiso);

        assertTrue(service.puedeSubmodulo(authentication, "CATALOGOS-CLINICA", AccionPermiso.LEER));
        assertFalse(service.puedeSubmodulo(authentication, "CATALOGOS-FARMACIA", AccionPermiso.LEER));
        assertTrue(service.puedeModulo(authentication, "CATALOGOS", AccionPermiso.LEER));
    }

    @Test
    void puede_combinaReferenciasDeModuloYSubmodulo() {
        ModuloEntity modulo = modulo("PACIENTES");
        SubmoduloEntity pacientes = submodulo(4L, modulo, "PACIENTES");
        RolPermisoEntity permiso = permiso(pacientes, true, false, false, false);
        PermissionService service = serviceCon("director", permiso);

        assertTrue(service.puede(authentication, Set.of("MEDICA"), Set.of("PACIENTES"), AccionPermiso.LEER));
        assertFalse(service.puede(authentication, Set.of("MEDICA"), Set.of("PACIENTES"), AccionPermiso.CREAR));
    }

    private PermissionService serviceCon(String username, RolPermisoEntity... permisos) {
        RolEntity rol = RolEntity.builder()
                .nombre("Rol")
                .permisos(List.of(permisos))
                .build();
        UsuarioEntity usuario = UsuarioEntity.builder()
                .username(username)
                .roles(Set.of(rol))
                .build();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(usuarioRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(usuario));
        return new PermissionService(usuarioRepository);
    }

    private RolPermisoEntity permiso(SubmoduloEntity submodulo, boolean leer, boolean crear, boolean editar,
            boolean eliminar) {
        return RolPermisoEntity.builder()
                .submodulo(submodulo)
                .puedeLeer(leer)
                .puedeCrear(crear)
                .puedeEditar(editar)
                .puedeEliminar(eliminar)
                .build();
    }

    private ModuloEntity modulo(String codigo) {
        return ModuloEntity.builder().id(1L).codigo(codigo).nombre(codigo).build();
    }

    private SubmoduloEntity submodulo(Long id, ModuloEntity modulo, String codigo) {
        return SubmoduloEntity.builder().id(id).modulo(modulo).codigo(codigo).nombre(codigo).build();
    }
}
