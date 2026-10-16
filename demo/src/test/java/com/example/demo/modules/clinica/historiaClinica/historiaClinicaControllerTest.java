package com.example.demo.modules.clinica.historiaClinica;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(historiaClinicaController.class)
class historiaClinicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 9, 30);

    @MockitoBean
    private historiaClinicaService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new historiaClinicaDTOs.Response(
                10L, "Paciente estable", 1L, 2L, "Carlos Mendez", FECHA, true));

        mockMvc.perform(post("/api/v1/historiaClinica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new historiaClinicaDTOs.Request("Paciente estable", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.descripcion").value("Paciente estable"))
                .andExpect(jsonPath("$.episodioId").value(1))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.usuarioNombre").value("Carlos Mendez"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void crear_debeRechazarDescripcionVacia() throws Exception {
        String json = "{\"descripcion\":\"\",\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/historiaClinica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarActivoTruePorDefecto() throws Exception {
        when(service.obtenerTodos(true)).thenReturn(List.of(
                new historiaClinicaDTOs.Response(1L, "Control", 1L, 2L, "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/historiaClinica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].episodioId").value(1))
                .andExpect(jsonPath("$[0].estado").value(true));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, true)).thenReturn(List.of(
                new historiaClinicaDTOs.Response(7L, "Control", 5L, 2L, "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/historiaClinica/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(7L)).thenReturn(
                new historiaClinicaDTOs.Response(7L, "Control", 1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(get("/api/v1/historiaClinica/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new historiaClinicaDTOs.Response(
                7L, "Paciente en observación", 1L, 3L, "Ana Lopez", FECHA, true));

        mockMvc.perform(put("/api/v1/historiaClinica/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new historiaClinicaDTOs.Request("Paciente en observación", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Paciente en observación"))
                .andExpect(jsonPath("$.usuarioId").value(3));
    }

    @Test
    void cambiarEstado_debeRetornarElEstadoAlternado() throws Exception {
        when(service.cambiarEstado(7L)).thenReturn(
                new historiaClinicaDTOs.Response(7L, "Paciente estable", 1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(patch("/api/v1/historiaClinica/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value(false));
    }
}
