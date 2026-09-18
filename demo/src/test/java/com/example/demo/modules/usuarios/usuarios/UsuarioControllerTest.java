package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.puestoController;
import com.example.demo.modules.usuarios.puesto.puestoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest({UsuarioController.class, puestoController.class})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private puestoService puestoService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_DeberiaRetornarCreated() throws Exception {
        UsuarioDTOs.Request request = new UsuarioDTOs.Request(
                new UsuarioDTOs.PersonaRequest("1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", true
        );

        when(usuarioService.crear(any(UsuarioDTOs.Request.class))).thenReturn("123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("123456"));
    }

    @Test
    void crear_DeberiaRetornarBadRequest_CuandoCuiInvalido() throws Exception {
        UsuarioDTOs.Request request = new UsuarioDTOs.Request(
                new UsuarioDTOs.PersonaRequest("123", "Juan", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", true
        );

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.obtenerPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jperez"));
    }

    @Test
    void actualizar_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.UpdateRequest request = new UsuarioDTOs.UpdateRequest(
                new UsuarioDTOs.PersonaRequest("1234567890123", "Juan Carlos", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", null, null, true
        );

        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan Carlos", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.actualizar(eq(1L), any(UsuarioDTOs.UpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"));
    }

    @Test
    void cambiarEstado_DeberiaRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.obtenerTodos(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("USR-01"));
    }

    @Test
    void buscar_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.buscarPorCriterio(eq("Juan"), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios/buscar").param("criterio", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }
}
