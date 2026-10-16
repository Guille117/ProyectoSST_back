package com.example.demo.modules.clinica.diagnostico;

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
@WebMvcTest(diagnosticoController.class)
class diagnosticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @MockitoBean
    private diagnosticoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new diagnosticoDTOs.Response(
                30L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", 1L, 2L, "Carlos Mendez", FECHA, true));

        mockMvc.perform(post("/api/v1/diagnostico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new diagnosticoDTOs.Request(TipoDiagnostico.PRINCIPAL, "Neumonía leve", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.tipoDiagnostico").value("PRINCIPAL"))
                .andExpect(jsonPath("$.descripcion").value("Neumonía leve"))
                .andExpect(jsonPath("$.episodioId").value(1))
                .andExpect(jsonPath("$.usuarioNombre").value("Carlos Mendez"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void crear_debeRechazarRequestSinTipoDeDiagnostico() throws Exception {
        String json = "{\"descripcion\":\"Neumonía leve\",\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/diagnostico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarActivoTruePorDefecto() throws Exception {
        when(service.obtenerTodos(true)).thenReturn(List.of(new diagnosticoDTOs.Response(
                30L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", 1L, 2L, "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/diagnostico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].tipoDiagnostico").value("PRINCIPAL"))
                .andExpect(jsonPath("$[0].estado").value(true));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, true)).thenReturn(List.of(new diagnosticoDTOs.Response(
                31L, TipoDiagnostico.PROVISIONAL, "Faringitis", 5L, 2L, "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/diagnostico/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31))
                .andExpect(jsonPath("$[0].tipoDiagnostico").value("PROVISIONAL"))
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void buscar_debeFiltrarPorTipoDeDiagnostico() throws Exception {
        when(service.buscarPorTipo(TipoDiagnostico.ASOCIADO, true)).thenReturn(List.of(new diagnosticoDTOs.Response(
                32L, TipoDiagnostico.ASOCIADO, "Hipertensión", 1L, 2L, "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/diagnostico/buscar").param("tipoDiagnostico", "ASOCIADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(32))
                .andExpect(jsonPath("$[0].tipoDiagnostico").value("ASOCIADO"));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(30L)).thenReturn(new diagnosticoDTOs.Response(
                30L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", 1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(get("/api/v1/diagnostico/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new diagnosticoDTOs.Response(
                30L, TipoDiagnostico.PRINCIPAL, "Neumonía confirmada", 1L, 3L, "Ana Lopez", FECHA, true));

        mockMvc.perform(put("/api/v1/diagnostico/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new diagnosticoDTOs.Request(TipoDiagnostico.PRINCIPAL, "Neumonía confirmada", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Neumonía confirmada"))
                .andExpect(jsonPath("$.usuarioId").value(3));
    }

    @Test
    void cambiarEstado_debeRetornarElEstadoAlternado() throws Exception {
        when(service.cambiarEstado(30L)).thenReturn(new diagnosticoDTOs.Response(
                30L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", 1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(patch("/api/v1/diagnostico/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value(false));
    }
}
