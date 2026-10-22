package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
@WebMvcTest(signosVitalesController.class)
class signosVitalesControllerTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private signosVitalesService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(service.crear(any())).thenReturn(new signosVitalesDTOs.Response(
                10L, 120, 80, 82, 18, new BigDecimal("36.8"), 98, "Paciente estable",
                1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(post("/api/v1/enfermeria/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new signosVitalesDTOs.Request(
                                120, 80, 82, 18, new BigDecimal("36.8"), 98, "Paciente estable", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.sistolica").value(120))
                .andExpect(jsonPath("$.usuarioNombre").value("Carlos Mendez"));
    }

    @Test
    void crear_debeRechazarSaturacionFueraDeRango() throws Exception {
        mockMvc.perform(post("/api/v1/enfermeria/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistolica\":120,\"diastolica\":80,\"frecuenciaCardiaca\":82,"
                                + "\"frecuenciaRespiratoria\":18,\"temperatura\":36.8,\"saturacion\":120,"
                                + "\"episodioId\":1,\"usuarioId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_debeRetornarLosSignosDelEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L)).thenReturn(List.of(new signosVitalesDTOs.Response(
                10L, 120, 80, 82, 18, new BigDecimal("36.8"), 98, null,
                5L, 2L, "Carlos Mendez", FECHA, false)));

        mockMvc.perform(get("/api/v1/enfermeria/signos-vitales/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new signosVitalesDTOs.Response(
                10L, 130, 85, 90, 20, new BigDecimal("37.2"), 96, "Control",
                1L, 3L, "Ana Lopez", FECHA, false));

        mockMvc.perform(put("/api/v1/enfermeria/signos-vitales/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new signosVitalesDTOs.Request(
                                130, 85, 90, 20, new BigDecimal("37.2"), 96, "Control", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistolica").value(130));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/enfermeria/signos-vitales/10"))
                .andExpect(status().isNoContent());
    }
}
