package com.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(AuthController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_DeberiaRetornarOk_CuandoCredencialesValidas() throws Exception {
        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest("jperez", "password123");
        AuthDTOs.AuthResponse response = new AuthDTOs.AuthResponse(
                "jwt-token", "Bearer", 1L, "USR-01", "jperez",
                "Juan Perez", "Doctor", List.of("Doctor"), true,
                List.of(new AuthDTOs.PermisoDTO(1L, "ROLES", "Roles", "USUARIOS", "Usuarios", true, true, false, false))
        );

        when(authService.login(any(AuthDTOs.LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.username").value("jperez"))
                .andExpect(jsonPath("$.permisos[0].puedeLeer").value(true));
    }

    @Test
    void login_DeberiaRetornarBadRequest_CuandoCamposInvalidos() throws Exception {
        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_DeberiaRetornarForbidden_CuandoFueraDeHorario() throws Exception {
        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest("jperez", "password123");

        when(authService.login(any(AuthDTOs.LoginRequest.class))).thenThrow(new HorarioAccessException("Acceso denegado: Fuera del horario de trabajo asignado"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acceso denegado: Fuera del horario de trabajo asignado"));
    }
}
