package com.example.demo.modules.usuarios.configuracion;

import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.example.demo.modules.usuarios.usuarios.UsuarioDTOs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(ConfiguracionController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class ConfiguracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ConfiguracionService configuracionService;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    private UsuarioDTOs.Response perfil() {
        return new UsuarioDTOs.Response(
                1L,
                "USR-01",
                "jperez",
                "Juan",
                "Perez",
                "1234567890123",
                Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1),
                "12345678",
                "juan@test.com",
                new UsuarioDTOs.PuestoResponse(1L, "Director"),
                new UsuarioDTOs.HorarioResponse(1L, "Diurno"),
                List.of(1L),
                List.of("Administrador"),
                true,
                null);
    }

    private UsuarioDTOs.PersonaRequest personaRequest(String cui) {
        return new UsuarioDTOs.PersonaRequest(
                cui,
                "Juan",
                "Perez",
                Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1),
                "12345678",
                "juan@test.com");
    }

    @Test
    void obtenerPerfil_deberiaRetornarOk() throws Exception {
        when(configuracionService.obtenerPerfil()).thenReturn(perfil());

        mockMvc.perform(get("/api/v1/configuracion/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jperez"))
                .andExpect(jsonPath("$.puesto.nombre").value("Director"));
    }

    @Test
    void actualizarPerfil_deberiaRetornarOk() throws Exception {
        when(configuracionService.actualizarPerfil(any(UsuarioDTOs.PersonaRequest.class)))
                .thenReturn(perfil());

        mockMvc.perform(put("/api/v1/configuracion/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest("1234567890123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"));
    }

    @Test
    void actualizarPerfil_deberiaRetornarBadRequest_CuandoCuiInvalido() throws Exception {
        mockMvc.perform(put("/api/v1/configuracion/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest("123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarUsuario_deberiaRetornarElTokenNuevo() throws Exception {
        ConfiguracionDTOs.CambioUsuarioRequest request =
                new ConfiguracionDTOs.CambioUsuarioRequest("jperez2");
        when(configuracionService.cambiarUsuario(any(ConfiguracionDTOs.CambioUsuarioRequest.class)))
                .thenReturn(new ConfiguracionDTOs.CambioUsuarioResponse("token-nuevo", "jperez2"));

        mockMvc.perform(put("/api/v1/configuracion/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-nuevo"))
                .andExpect(jsonPath("$.username").value("jperez2"));
    }

    @Test
    void cambiarUsuario_deberiaRetornarBadRequest_CuandoUsernameCorto() throws Exception {
        mockMvc.perform(put("/api/v1/configuracion/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarPassword_deberiaRetornarNoContent() throws Exception {
        ConfiguracionDTOs.CambioPasswordRequest request =
                new ConfiguracionDTOs.CambioPasswordRequest("actual123", "nueva123", "nueva123");

        mockMvc.perform(put("/api/v1/configuracion/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(configuracionService).cambiarPassword(any(ConfiguracionDTOs.CambioPasswordRequest.class));
    }

    @Test
    void cambiarPassword_deberiaRetornarBadRequest_CuandoNuevaEsCorta() throws Exception {
        mockMvc.perform(put("/api/v1/configuracion/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"actual\",\"passwordNueva\":\"123\",\"confirmarPassword\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }
}
