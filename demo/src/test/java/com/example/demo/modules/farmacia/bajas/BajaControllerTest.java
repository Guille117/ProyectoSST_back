package com.example.demo.modules.farmacia.bajas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BajaController.class)
class BajaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BajaService service;

    private String jsonValido() {
        return """
                {
                  "idLote": 1,
                  "cantidad": 4,
                  "idMotivoBaja": 2,
                  "observacion": "producto dañado",
                  "idUsuario": 3
                }
                """;
    }

    @Test
    void registrar_debeRetornarCreated() throws Exception {
        BajaDTOs.Response resp = new BajaDTOs.Response(
                99L, 1L, "LT-001", 4, 10, 6, 2L, "Vencimiento",
                "producto dañado", LocalDateTime.now(), 3L, "jperez");
        when(service.registrar(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/bajas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.codigoLote").value("LT-001"))
                .andExpect(jsonPath("$.cantidad").value(4))
                .andExpect(jsonPath("$.stockAnterior").value(10))
                .andExpect(jsonPath("$.stockActual").value(6))
                .andExpect(jsonPath("$.motivo").value("Vencimiento"))
                .andExpect(jsonPath("$.usuario").value("jperez"));
    }

    @Test
    void registrar_debeRetornarBadRequestSinCamposObligatorios() throws Exception {
        String json = "{\"observacion\":\"sin datos\"}";

        mockMvc.perform(post("/api/v1/bajas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_debeRetornarOk() throws Exception {
        when(service.listar(null)).thenReturn(List.of(new BajaDTOs.ListadoResponse(
                99L, "Penicilina Bayer Tabletas 500mg", "LT-001", "Vencimiento",
                "producto dañado", LocalDateTime.now(), "Juan Pérez López")));

        mockMvc.perform(get("/api/v1/bajas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idBaja").value(99))
                .andExpect(jsonPath("$[0].producto").value("Penicilina Bayer Tabletas 500mg"))
                .andExpect(jsonPath("$[0].lote").value("LT-001"))
                .andExpect(jsonPath("$[0].motivo").value("Vencimiento"))
                .andExpect(jsonPath("$[0].usuario").value("Juan Pérez López"));
    }

    @Test
    void listar_debeFiltrarPorTipo() throws Exception {
        when(service.listar("insumo")).thenReturn(List.of(new BajaDTOs.ListadoResponse(
                100L, "Jeringa descartable Bayer 5 ml", "Tk-020", "Daño",
                null, LocalDateTime.now(), "Juan Pérez López")));

        mockMvc.perform(get("/api/v1/bajas").param("tipo", "insumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].producto").value("Jeringa descartable Bayer 5 ml"))
                .andExpect(jsonPath("$[0].lote").value("Tk-020"));
    }

    @Test
    void revertir_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/bajas/99"))
                .andExpect(status().isNoContent());
    }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}
