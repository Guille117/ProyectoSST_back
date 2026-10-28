package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.pacientes.paciente.TipoAtencion;
import com.example.demo.modules.usuarios.usuarios.Sexo;
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
@WebMvcTest(altaController.class)
class altaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 9, 30);

    @MockitoBean
    private altaService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElAltaGuardada() throws Exception {
        when(service.crear(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/altas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Diagnóstico"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.tipoAlta").value("INDICACION_MEDICA"))
                .andExpect(jsonPath("$.diagnosticoEgreso").value("Diagnóstico"))
                .andExpect(jsonPath("$.condicionEgresoNombre").value("Estable"))
                .andExpect(jsonPath("$.estado").value("VIGENTE"))
                .andExpect(jsonPath("$.tratamientos[0].nombre").value("Ibuprofeno"));
    }

    @Test
    void crear_debeRechazarDiagnosticoVacio() throws Exception {
        String json = "{\"tipoAlta\":\"INDICACION_MEDICA\",\"diagnosticoEgreso\":\"\","
                + "\"episodioId\":1,\"usuarioId\":2,\"tratamientos\":[]}";

        mockMvc.perform(post("/api/v1/altas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/altas/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].estado").value("VIGENTE"));
    }

    @Test
    void recientes_debeRetornarLasAltasDeLasUltimasHoras() throws Exception {
        when(service.recientes(48)).thenReturn(List.of(new altaDTOs.RecienteResponse(
                10L,
                TipoAlta.RETIRO_VOLUNTARIO,
                FECHA,
                "Diagnóstico",
                "Estable",
                "Observación",
                ResponsableRetiro.PACIENTE,
                "Motivo",
                List.of("Riesgo"),
                null,
                null,
                null,
                1L,
                5L,
                "EXP-001",
                "Carlos Mendez",
                "1234567890123",
                Sexo.MASCULINO,
                "70000001",
                TipoAtencion.HOSPITALIZACION,
                45)));

        mockMvc.perform(get("/api/v1/altas/recientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].altaId").value(10))
                .andExpect(jsonPath("$[0].codigoExpediente").value("EXP-001"))
                .andExpect(jsonPath("$[0].tipoAlta").value("RETIRO_VOLUNTARIO"))
                .andExpect(jsonPath("$[0].edad").value(45));
    }

    @Test
    void obtenerPorId_debeRetornarElAlta() throws Exception {
        when(service.obtenerPorId(10L)).thenReturn(response());

        mockMvc.perform(get("/api/v1/altas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.episodioId").value(1));
    }

    @Test
    void actualizar_debeRetornarElAltaActualizada() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(response());

        mockMvc.perform(put("/api/v1/altas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                request("Diagnóstico actualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.diagnosticoEgreso").value("Diagnóstico"));
    }

    @Test
    void anular_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/altas/10"))
                .andExpect(status().isNoContent());
    }

    private altaDTOs.Request request(String diagnostico) {
        return new altaDTOs.Request(
                TipoAlta.INDICACION_MEDICA,
                diagnostico,
                2L,
                "Observación",
                1L,
                2L,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                List.of(new altaDTOs.TratamientoRequest(
                        TipoTratamiento.MEDICAMENTO,
                        "Ibuprofeno",
                        2,
                        "tabletas",
                        8,
                        TipoDuracion.DIAS,
                        7,
                        "Con alimentos",
                        9L,
                        null)));
    }

    private altaDTOs.Response response() {
        return new altaDTOs.Response(
                10L,
                TipoAlta.INDICACION_MEDICA,
                "Diagnóstico",
                2L,
                "Estable",
                "Observación",
                FECHA,
                1L,
                2L,
                "Carlos Mendez",
                EstadoAlta.VIGENTE,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                null,
                List.of(new altaDTOs.TratamientoResponse(
                        TipoTratamiento.MEDICAMENTO,
                        "Ibuprofeno",
                        2,
                        "tabletas",
                        8,
                        TipoDuracion.DIAS,
                        7,
                        "Con alimentos",
                        9L,
                        null)));
    }
}
