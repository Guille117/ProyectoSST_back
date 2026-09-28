package com.example.demo.modules.usuarios.roles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(RolController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private RolService service;

    @MockitoBean
    private RolMapper mapper;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void obtenerPorId_DeberiaRetornarOk() throws Exception {
        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Administrador", true, List.of());

        when(service.obtenerPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Administrador"));
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Administrador", true, List.of());

        when(service.obtenerTodos(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ROL-01"));
    }

    @Test
    void buscar_DeberiaRetornarOk() throws Exception {
        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Administrador", true, List.of());

        when(service.buscarPorCriterio(eq("Admin"), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/roles/buscar")
                        .param("criterio", "Admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Administrador"));
    }

    @Test
    void obtenerModulos_DeberiaRetornarOk() throws Exception {
        RolDTOs.ModuloResponse mod = new RolDTOs.ModuloResponse(
                1L, "USUARIOS", "Usuarios", true,
                List.of(new RolDTOs.SubmoduloResponse(1L, "ROLES", "Roles", true))
        );

        when(service.obtenerModulosJerarquicos()).thenReturn(List.of(mod));

        mockMvc.perform(get("/api/v1/roles/modulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("USUARIOS"))
                .andExpect(jsonPath("$[0].submodulos[0].codigo").value("ROLES"));
    }
}
