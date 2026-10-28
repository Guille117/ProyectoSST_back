package com.example.demo.modules.clinica.evolucion;

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
@WebMvcTest(evolucionController.class)
class evolucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 9, 30);

    @MockitoBean
    private evolucionService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new evolucionDTOs.Response(
                10L, "Paciente estable", "Continuar tratamiento", 1L, 2L,
                "Carlos Mendez", FECHA, EstadoEvolucion.ACTIVA));

        mockMvc.perform(post("/api/v1/evolucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new evolucionDTOs.Request(
                                "Paciente estable", "Continuar tratamiento", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.evolucionClinica").value("Paciente estable"))
                .andExpect(jsonPath("$.planMedico").value("Continuar tratamiento"))
                .andExpect(jsonPath("$.episodioId").value(1))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.usuarioNombre").value("Carlos Mendez"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void crear_debeRechazarEvolucionClinicaVacia() throws Exception {
        String json = "{\"evolucionClinica\":\"\",\"planMedico\":\"Plan\","
                + "\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/evolucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_debeAplicarEstadoActivaPorDefecto() throws Exception {
        when(service.buscarPorEpisodio(5L, EstadoEvolucion.ACTIVA)).thenReturn(List.of(
                new evolucionDTOs.Response(7L, "Control", "Plan", 5L, 2L,
                        "Carlos Mendez", FECHA, EstadoEvolucion.ACTIVA)));

        mockMvc.perform(get("/api/v1/evolucion/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].episodioId").value(5))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    void buscar_debeFiltrarPorEstadoEliminada() throws Exception {
        when(service.buscarPorEpisodio(5L, EstadoEvolucion.ELIMINADA)).thenReturn(List.of(
                new evolucionDTOs.Response(8L, "Control", "Plan", 5L, 2L,
                        "Carlos Mendez", FECHA, EstadoEvolucion.ELIMINADA)));

        mockMvc.perform(get("/api/v1/evolucion/buscar")
                        .param("episodioId", "5")
                        .param("estado", "ELIMINADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].estado").value("ELIMINADA"));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(7L)).thenReturn(new evolucionDTOs.Response(
                7L, "Control", "Plan", 1L, 2L, "Carlos Mendez", FECHA,
                EstadoEvolucion.ACTIVA));

        mockMvc.perform(get("/api/v1/evolucion/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new evolucionDTOs.Response(
                7L, "Paciente en observación", "Nuevo plan", 1L, 3L,
                "Ana Lopez", FECHA, EstadoEvolucion.ACTIVA));

        mockMvc.perform(put("/api/v1/evolucion/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new evolucionDTOs.Request(
                                "Paciente en observación", "Nuevo plan", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evolucionClinica").value("Paciente en observación"))
                .andExpect(jsonPath("$.planMedico").value("Nuevo plan"))
                .andExpect(jsonPath("$.usuarioId").value(3));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/evolucion/7"))
                .andExpect(status().isNoContent());
    }
}
