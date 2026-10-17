package com.example.demo.modules.clinica.indicaciones.medicamentos;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(indicacionMedicamentoController.class)
class indicacionMedicamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @MockitoBean
    private indicacionMedicamentoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new indicacionMedicamentoDTOs.Response(
                30L, 9L, "Ibuprofeno", 2, 8, 7, TipoDuracion.DIAS, "Con alimentos",
                1L, 2L, "Carlos Mendez", FECHA, EstadoIndicacionMedicamento.ACTIVO));

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new indicacionMedicamentoDTOs.Request(
                                9L, 2, 8, 7, TipoDuracion.DIAS, "Con alimentos", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.medicamentoLogId").value(9))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.tipoDuracion").value("DIAS"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void crear_debeRechazarRequestSinMedicamento() throws Exception {
        String json = "{\"cantidad\":2,\"frecuencia\":8,\"duracion\":7,\"tipoDuracion\":\"DIAS\","
                + "\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarEstadoActivoPorDefecto() throws Exception {
        when(service.obtenerTodos(EstadoIndicacionMedicamento.ACTIVO)).thenReturn(List.of(new indicacionMedicamentoDTOs.Response(
                30L, 9L, "Ibuprofeno", 2, 8, 7, TipoDuracion.DIAS, "Con alimentos",
                1L, 2L, "Carlos Mendez", FECHA, EstadoIndicacionMedicamento.ACTIVO)));

        mockMvc.perform(get("/api/v1/medicamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, EstadoIndicacionMedicamento.ACTIVO))
                .thenReturn(List.of(new indicacionMedicamentoDTOs.Response(
                        31L, 9L, "Ibuprofeno", 1, 12, 3, TipoDuracion.DIAS, null,
                        5L, 2L, "Carlos Mendez", FECHA, EstadoIndicacionMedicamento.ACTIVO)));

        mockMvc.perform(get("/api/v1/medicamentos/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(30L)).thenReturn(new indicacionMedicamentoDTOs.Response(
                30L, 9L, "Ibuprofeno", 2, 8, 7, TipoDuracion.DIAS, "Con alimentos",
                1L, 2L, "Carlos Mendez", FECHA, EstadoIndicacionMedicamento.FINALIZADO));

        mockMvc.perform(get("/api/v1/medicamentos/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new indicacionMedicamentoDTOs.Response(
                30L, 10L, "Paracetamol", 3, 6, null, TipoDuracion.TIEMPO_INDEFINIDO, "Nuevo",
                1L, 3L, "Ana Lopez", FECHA, EstadoIndicacionMedicamento.ACTIVO));

        mockMvc.perform(put("/api/v1/medicamentos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new indicacionMedicamentoDTOs.Request(
                                10L, 3, 6, null, TipoDuracion.TIEMPO_INDEFINIDO, "Nuevo", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamentoLogId").value(10))
                .andExpect(jsonPath("$.tipoDuracion").value("TIEMPO_INDEFINIDO"));
    }

    @Test
    void cambiarEstado_debeRetornarElNuevoEstado() throws Exception {
        when(service.cambiarEstado(30L, EstadoIndicacionMedicamento.FINALIZADO))
                .thenReturn(new indicacionMedicamentoDTOs.Response(
                        30L, 9L, "Ibuprofeno", 2, 8, 7, TipoDuracion.DIAS, "Con alimentos",
                        1L, 2L, "Carlos Mendez", FECHA, EstadoIndicacionMedicamento.FINALIZADO));

        mockMvc.perform(patch("/api/v1/medicamentos/30/estado").param("estado", "FINALIZADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/medicamentos/30"))
                .andExpect(status().isNoContent());
    }
}
