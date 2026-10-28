package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Set;

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

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @Test
    void administradorPuedeCrearEditarYEliminarEnElSubmodulo() {
        SubmoduloEntity submodulo = SubmoduloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").build();
        RolPermisoEntity permiso = RolPermisoEntity.builder()
                .submodulo(submodulo)
                .puedeLeer(true)
                .puedeCrear(true)
                .puedeEditar(true)
                .puedeEliminar(true)
                .build();
        RolEntity administrador = RolEntity.builder()
                .nombre("Administrador")
                .permisos(new ArrayList<>(java.util.List.of(permiso)))
                .build();
        UsuarioEntity usuario = UsuarioEntity.builder().username("admin").roles(Set.of(administrador)).build();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(java.util.Optional.of(usuario));
        PermissionService service = new PermissionService(usuarioRepository);

        assertTrue(service.canCreate(authentication, "USUARIOS"));
        assertTrue(service.canEdit(authentication, "USUARIOS"));
        assertTrue(service.canDelete(authentication, "USUARIOS"));
    }

    @Test
    void rolSinPermisosNoPuedeCrearEditarNiEliminar() {
        RolEntity medico = RolEntity.builder().nombre("Medico").build();
        UsuarioEntity usuario = UsuarioEntity.builder().username("medico").roles(Set.of(medico)).build();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("medico");
        when(usuarioRepository.findByUsernameIgnoreCase("medico")).thenReturn(java.util.Optional.of(usuario));
        PermissionService service = new PermissionService(usuarioRepository);

        assertFalse(service.canCreate(authentication, "USUARIOS"));
        assertFalse(service.canEdit(authentication, "USUARIOS"));
        assertFalse(service.canDelete(authentication, "USUARIOS"));
    }
}