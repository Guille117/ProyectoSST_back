package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

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
@WebMvcTest(controlHidricoController.class)
class controlHidricoControllerTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private controlHidricoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(service.crear(any())).thenReturn(new controlHidricoDTOs.Response(
                10L, TipoRegistroHidrico.INGRESO, "Oral", 500, "Agua",
                1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(post("/api/v1/enfermeria/control-hidrico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new controlHidricoDTOs.Request(
                                TipoRegistroHidrico.INGRESO, "Oral", 500, "Agua", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.tipo").value("INGRESO"))
                .andExpect(jsonPath("$.cantidad").value(500));
    }

    @Test
    void crear_debeRechazarTipoInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/enfermeria/control-hidrico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"OTRO\",\"via\":\"Oral\",\"cantidad\":500,"
                                + "\"descripcion\":\"Agua\",\"episodioId\":1,\"usuarioId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_debeRetornarLosRegistrosDelEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L)).thenReturn(List.of(new controlHidricoDTOs.Response(
                10L, TipoRegistroHidrico.INGRESO, "Oral", 500, "Agua",
                5L, 2L, "Carlos Mendez", FECHA, false)));

        mockMvc.perform(get("/api/v1/enfermeria/control-hidrico/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new controlHidricoDTOs.Response(
                10L, TipoRegistroHidrico.EGRESO, "Sonda vesical", 400, "Orina",
                1L, 3L, "Ana Lopez", FECHA, false));

        mockMvc.perform(put("/api/v1/enfermeria/control-hidrico/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new controlHidricoDTOs.Request(
                                TipoRegistroHidrico.EGRESO, "Sonda vesical", 400, "Orina", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("EGRESO"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/enfermeria/control-hidrico/10"))
                .andExpect(status().isNoContent());
    }
}
