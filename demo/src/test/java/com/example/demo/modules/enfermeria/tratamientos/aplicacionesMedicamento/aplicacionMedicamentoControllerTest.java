package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

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
@WebMvcTest(aplicacionMedicamentoController.class)
class aplicacionMedicamentoControllerTest {

    private static final LocalDateTime PROGRAMADA = LocalDateTime.of(2026, 10, 16, 8, 0);
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 16, 8, 5);

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private aplicacionMedicamentoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(service.crear(any())).thenReturn(new aplicacionMedicamentoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoAplicacionMedicamento.ADMINISTRADO, 1,
                "Administrado", 2L, "Enf. María López", REGISTRO));

        mockMvc.perform(post("/api/v1/enfermeria/aplicaciones-medicamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new aplicacionMedicamentoDTOs.Request(
                                7L, PROGRAMADA, EstadoAplicacionMedicamento.ADMINISTRADO, 1,
                                "Administrado", 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.estado").value("ADMINISTRADO"));
    }

    @Test
    void crear_debeRechazarRequestSinIndicacion() throws Exception {
        mockMvc.perform(post("/api/v1/enfermeria/aplicaciones-medicamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaHoraProgramada\":\"2026-10-16T08:00:00\","
                                + "\"estado\":\"ADMINISTRADO\",\"usuarioId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorIndicacion_debeRetornarLasAplicaciones() throws Exception {
        when(service.buscar(7L, null)).thenReturn(List.of(new aplicacionMedicamentoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoAplicacionMedicamento.ADMINISTRADO, 1,
                null, 2L, "Enf. María López", REGISTRO)));

        mockMvc.perform(get("/api/v1/enfermeria/aplicaciones-medicamento/buscar")
                        .param("indicacionId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].indicacionMedicamentoId").value(7));
    }

    @Test
    void actualizar_debeRetornarLaAplicacionActualizada() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new aplicacionMedicamentoDTOs.Response(
                20L, 7L, PROGRAMADA, EstadoAplicacionMedicamento.NO_ADMINISTRADO, null,
                "Paciente en ayunas", 3L, "Enf. Carlos Ramírez", REGISTRO));

        mockMvc.perform(put("/api/v1/enfermeria/aplicaciones-medicamento/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new aplicacionMedicamentoDTOs.Request(
                                7L, PROGRAMADA, EstadoAplicacionMedicamento.NO_ADMINISTRADO, null,
                                "Paciente en ayunas", 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NO_ADMINISTRADO"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/enfermeria/aplicaciones-medicamento/20"))
                .andExpect(status().isNoContent());
    }
}
