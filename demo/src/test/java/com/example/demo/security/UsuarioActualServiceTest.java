package com.example.demo.security;

import com.example.demo.modules.usuarios.roles.PermisosSistema;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioActualServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioActualService service;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void medicoRestringidoId_devuelveIdCuandoElUsuarioSoloTieneRolMedico() {
        autenticar("medico");
        when(usuarioRepository.findByUsernameIgnoreCase("medico"))
                .thenReturn(Optional.of(usuario(5L, PermisosSistema.ROL_MEDICO)));

        assertEquals(Optional.of(5L), service.medicoRestringidoId());
    }

    @Test
    void medicoRestringidoId_vacioCuandoTieneUnRolConAccesoGlobal() {
        autenticar("director");
        when(usuarioRepository.findByUsernameIgnoreCase("director"))
                .thenReturn(Optional.of(usuario(7L, PermisosSistema.ROL_MEDICO,
                        PermisosSistema.ROL_DIRECTOR)));

        assertTrue(service.medicoRestringidoId().isEmpty());
    }

    @Test
    void medicoRestringidoId_vacioCuandoNoEsMedico() {
        autenticar("enfermera");
        when(usuarioRepository.findByUsernameIgnoreCase("enfermera"))
                .thenReturn(Optional.of(usuario(9L, PermisosSistema.ROL_ENFERMERA)));

        assertTrue(service.medicoRestringidoId().isEmpty());
    }

    @Test
    void medicoRestringidoId_vacioSinAutenticacion() {
        assertTrue(service.medicoRestringidoId().isEmpty());
    }

    private void autenticar(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, java.util.List.of()));
    }

    private UsuarioEntity usuario(Long id, String... roles) {
        Set<RolEntity> rolesUsuario = java.util.Arrays.stream(roles)
                .map(nombre -> RolEntity.builder().nombre(nombre).build())
                .collect(java.util.stream.Collectors.toSet());
        return UsuarioEntity.builder().id(id).roles(rolesUsuario).build();
    }
}
