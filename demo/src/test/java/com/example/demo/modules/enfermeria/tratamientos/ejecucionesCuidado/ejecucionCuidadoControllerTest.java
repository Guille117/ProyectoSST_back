package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ejecucionCuidadoController.class)
class ejecucionCuidadoControllerTest {

    private static final LocalDateTime PROGRAMADA = LocalDateTime.of(2026, 10, 16, 8, 0);
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 16, 8, 5);

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ejecucionCuidadoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(service.crear(any())).thenReturn(new ejecucionCuidadoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO,
                "Realizado", 2L, "Enf. María López", REGISTRO));

        mockMvc.perform(post("/api/v1/enfermeria/ejecuciones-cuidado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ejecucionCuidadoDTOs.Request(
                                7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO, "Realizado", 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.estado").value("REALIZADO"));
    }

    @Test
    void crear_debeRechazarRequestSinCuidado() throws Exception {
        mockMvc.perform(post("/api/v1/enfermeria/ejecuciones-cuidado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaHoraProgramada\":\"2026-10-16T08:00:00\","
                                + "\"estado\":\"REALIZADO\",\"usuarioId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorCuidado_debeRetornarLasEjecuciones() throws Exception {
        when(service.buscar(7L, null)).thenReturn(List.of(new ejecucionCuidadoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO,
                null, 2L, "Enf. María López", REGISTRO)));

        mockMvc.perform(get("/api/v1/enfermeria/ejecuciones-cuidado/buscar")
                        .param("cuidadoId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cuidadoId").value(7));
    }

    @Test
    void actualizar_debeRetornarLaEjecucionActualizada() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new ejecucionCuidadoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoEjecucionCuidado.NO_REALIZADO,
                "Paciente rechaza", 3L, "Enf. Carlos Ramírez", REGISTRO));

        mockMvc.perform(put("/api/v1/enfermeria/ejecuciones-cuidado/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ejecucionCuidadoDTOs.Request(
                                7L, PROGRAMADA, EstadoEjecucionCuidado.NO_REALIZADO,
                                "Paciente rechaza", 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NO_REALIZADO"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/enfermeria/ejecuciones-cuidado/20"))
                .andExpect(status().isNoContent());
    }
}
