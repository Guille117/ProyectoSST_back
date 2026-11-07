package com.example.demo.security;

import com.example.demo.modules.usuarios.puesto.puestoEntity;
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
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        HorarioLaboralService horarioLaboralService =
                new HorarioLaboralService(new UsuarioActualService(usuarioRepository), usuarioRepository,
                        Clock.system(ZoneId.of("America/Guatemala")));
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, horarioLaboralService);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deniegaPeticionDeUsuarioNoMedicoSinHorario() throws Exception {
        autenticarToken("enfermera");
        when(usuarioRepository.findByUsernameIgnoreCase("enfermera"))
                .thenReturn(Optional.of(usuario(1L, "Enfermera", PermisosSistema.ROL_ENFERMERA, null)));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pacientes");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("FUERA_DE_HORARIO"));
        assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void permitePeticionDeMedicoSinHorario() throws Exception {
        autenticarToken("medico");
        when(usuarioRepository.findByUsernameIgnoreCase("medico"))
                .thenReturn(Optional.of(usuario(2L, "Médico", PermisosSistema.ROL_MEDICO, null)));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pacientes");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void permiteCerrarSesionAunqueEsteFueraDeHorario() throws Exception {
        autenticarToken("enfermera");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/logout");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    private void autenticarToken(String username) {
        when(jwtUtil.validateToken("token-valido")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("token-valido")).thenReturn(username);
        UserDetails userDetails = User.withUsername(username).password("").authorities(java.util.List.of()).build();
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    }

    private UsuarioEntity usuario(Long id, String puesto, String rol, Object horario) {
        puestoEntity puestoEntity = new puestoEntity();
        puestoEntity.setNombre(puesto);
        puestoEntity.setEstado(true);
        return UsuarioEntity.builder()
                .id(id)
                .username(puesto.toLowerCase())
                .puesto(puestoEntity)
                .horario(null)
                .roles(Set.of(RolEntity.builder().nombre(rol).build()))
                .build();
    }
}
