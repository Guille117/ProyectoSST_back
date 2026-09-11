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
    void crear_DeberiaRetornarCreated() throws Exception {
        RolDTOs.PermisoRequest perm = new RolDTOs.PermisoRequest(1L, true, true, false, false);
        RolDTOs.Request request = new RolDTOs.Request("Administrador", true, List.of(perm));

        RolDTOs.Response response = new RolDTOs.Response(
                1L, "ROL-01", "Administrador", true,
                List.of(new RolDTOs.PermisoResponse(1L, 1L, "ROLES", "Roles", "USUARIOS", "Usuarios", true, true, false, false))
        );

        when(service.crear(any(RolDTOs.Request.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("ROL-01"))
                .andExpect(jsonPath("$.nombre").value("Administrador"));
    }

    @Test
    void crear_DeberiaRetornarBadRequest_CuandoNombreInvalido() throws Exception {
        RolDTOs.Request request = new RolDTOs.Request("", true, List.of());

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

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
    void actualizar_DeberiaRetornarOk() throws Exception {
        RolDTOs.PermisoRequest perm = new RolDTOs.PermisoRequest(1L, true, false, true, false);
        RolDTOs.Request request = new RolDTOs.Request("Rol Actualizado", true, List.of(perm));

        RolDTOs.Response response = new RolDTOs.Response(1L, "ROL-01", "Rol Actualizado", true, List.of());

        when(service.actualizar(eq(1L), any(RolDTOs.Request.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Rol Actualizado"));
    }

    @Test
    void cambiarEstado_DeberiaRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/roles/1"))
                .andExpect(status().isOk());
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
