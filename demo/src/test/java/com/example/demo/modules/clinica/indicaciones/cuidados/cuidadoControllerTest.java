package com.example.demo.modules.clinica.indicaciones.cuidados;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(cuidadoController.class)
class cuidadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private cuidadoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new cuidadoDTOs.Response(
                30L, "Cambio de vendaje", 12, 5, TipoDuracion.DIAS, "Cada turno",
                1L, 2L, "Carlos Mendez", EstadoCuidado.EN_PROCESO));

        mockMvc.perform(post("/api/v1/cuidados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new cuidadoDTOs.Request(
                                "Cambio de vendaje", 12, 5, TipoDuracion.DIAS, "Cada turno", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.nombreCuidado").value("Cambio de vendaje"))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    void crear_debeRechazarRequestSinNombre() throws Exception {
        String json = "{\"frecuencia\":12,\"duracion\":5,\"tipoDuracion\":\"DIAS\","
                + "\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/cuidados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarEstadoEnProcesoPorDefecto() throws Exception {
        when(service.obtenerTodos(EstadoCuidado.EN_PROCESO)).thenReturn(List.of(new cuidadoDTOs.Response(
                30L, "Cambio de vendaje", 12, 5, TipoDuracion.DIAS, "Cada turno",
                1L, 2L, "Carlos Mendez", EstadoCuidado.EN_PROCESO)));

        mockMvc.perform(get("/api/v1/cuidados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].estado").value("EN_PROCESO"));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, EstadoCuidado.EN_PROCESO))
                .thenReturn(List.of(new cuidadoDTOs.Response(
                        31L, "Curacion", 8, 3, TipoDuracion.DIAS, null,
                        5L, 2L, "Carlos Mendez", EstadoCuidado.EN_PROCESO)));

        mockMvc.perform(get("/api/v1/cuidados/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(30L)).thenReturn(new cuidadoDTOs.Response(
                30L, "Cambio de vendaje", 12, 5, TipoDuracion.DIAS, "Cada turno",
                1L, 2L, "Carlos Mendez", EstadoCuidado.COMPLETADO));

        mockMvc.perform(get("/api/v1/cuidados/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new cuidadoDTOs.Response(
                30L, "Cambio de sonda", 24, null, TipoDuracion.TIEMPO_INDEFINIDO, "Vigilar",
                1L, 3L, "Ana Lopez", EstadoCuidado.EN_PROCESO));

        mockMvc.perform(put("/api/v1/cuidados/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new cuidadoDTOs.Request(
                                "Cambio de sonda", 24, null, TipoDuracion.TIEMPO_INDEFINIDO, "Vigilar", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCuidado").value("Cambio de sonda"))
                .andExpect(jsonPath("$.tipoDuracion").value("TIEMPO_INDEFINIDO"));
    }

    @Test
    void cambiarEstado_debeRetornarElNuevoEstado() throws Exception {
        when(service.cambiarEstado(30L, EstadoCuidado.COMPLETADO)).thenReturn(new cuidadoDTOs.Response(
                30L, "Cambio de vendaje", 12, 5, TipoDuracion.DIAS, "Cada turno",
                1L, 2L, "Carlos Mendez", EstadoCuidado.COMPLETADO));

        mockMvc.perform(patch("/api/v1/cuidados/30/estado").param("estado", "COMPLETADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/cuidados/30"))
                .andExpect(status().isNoContent());
    }
}
