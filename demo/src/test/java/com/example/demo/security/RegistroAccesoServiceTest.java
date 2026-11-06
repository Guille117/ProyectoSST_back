package com.example.demo.security;

import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteRepository;
import com.example.demo.modules.usuarios.roles.PermisosSistema;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroAccesoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private EpisodioRepository episodioRepository;

    private RegistroAccesoService service;

    @BeforeEach
    void setUp() {
        UsuarioActualService usuarioActual = new UsuarioActualService(usuarioRepository);
        service = new RegistroAccesoService(usuarioActual, usuarioRepository, pacienteRepository,
                episodioRepository);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autorPuedeEditarSuPropioRegistro() {
        autenticar("medico");
        when(usuarioRepository.findByUsernameIgnoreCase("medico"))
                .thenReturn(Optional.of(usuario(1L, PermisosSistema.ROL_MEDICO)));

        assertDoesNotThrow(() -> service.validarEdicion(usuario(1L)));
    }

    @Test
    void otroUsuarioNoPuedeEditarUnRegistroAjeno() {
        autenticar("medico");
        when(usuarioRepository.findByUsernameIgnoreCase("medico"))
                .thenReturn(Optional.of(usuario(1L, PermisosSistema.ROL_MEDICO)));

        assertThrows(AccessDeniedException.class, () -> service.validarEdicion(usuario(2L)));
    }

    @Test
    void administradorPuedeEditarCualquierRegistro() {
        autenticar("admin");
        when(usuarioRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(usuario(9L, PermisosSistema.ROL_ADMINISTRADOR)));

        assertDoesNotThrow(() -> service.validarEdicion(usuario(2L)));
    }

    @Test
    void directorNoPuedeEditarUnRegistroAjeno() {
        autenticar("director");
        when(usuarioRepository.findByUsernameIgnoreCase("director"))
                .thenReturn(Optional.of(usuario(9L, PermisosSistema.ROL_DIRECTOR)));

        assertThrows(AccessDeniedException.class, () -> service.validarEdicion(usuario(2L)));
    }

    private void autenticar(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, java.util.List.of()));
    }

    private UsuarioEntity usuario(Long id, String... roles) {
        Set<RolEntity> rolesUsuario = Arrays.stream(roles)
                .map(nombre -> RolEntity.builder().nombre(nombre).build())
                .collect(Collectors.toSet());
        return UsuarioEntity.builder().id(id).roles(rolesUsuario).build();
    }
}
