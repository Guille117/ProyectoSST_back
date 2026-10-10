package com.example.demo.modules.farmacia.lotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(LoteController.class)
class LoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private LoteService service;

    private String jsonValido() {
        return """
                {
                  "idCompra": 1,
                  "idInsumoLog": 5,
                  "codigoLote": "LT-001",
                  "cantidad": 10,
                  "fechaVencimiento": "2027-06-30",
                  "precioCompra": 2.50,
                  "precioVenta": 5.00
                }
                """;
    }

    @Test
    void crear_debeRetornarCreated() throws Exception {
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("25.00"),
                5L, "Gasas", null, null, 1L, "COM-1");
        when(service.crear(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarCreatedSinFechaVencimientoParaInsumo() throws Exception {
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, null,
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("25.00"),
                5L, "Gasas", null, null, 1L, "COM-1");
        when(service.crear(any())).thenReturn(resp);

        String json = """
                {
                  "idCompra": 1,
                  "idInsumoLog": 5,
                  "codigoLote": "LT-001",
                  "cantidad": 10,
                  "precioCompra": 2.50,
                  "precioVenta": 5.00
                }
                """;

        mockMvc.perform(post("/api/v1/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarBadRequestSinCamposObligatorios() throws Exception {
        String json = "{\"idCompra\":1,\"codigoLote\":\"LT-001\"}";

        mockMvc.perform(post("/api/v1/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_debeRetornarBadRequestSinIdCompra() throws Exception {
        String json = """
                {
                  "idInsumoLog": 5,
                  "codigoLote": "LT-001",
                  "cantidad": 10,
                  "fechaVencimiento": "2027-06-30",
                  "precioCompra": 2.50,
                  "precioVenta": 5.00
                }
                """;

        mockMvc.perform(post("/api/v1/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_debeRetornarOk() throws Exception {
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("25.00"),
                5L, "Gasas", null, null, 1L, "COM-1");
        when(service.obtenerPorId(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/lotes/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_debeRetornarOk() throws Exception {
        when(service.obtenerTodos(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lotes"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizar_debeRetornarOk() throws Exception {
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 20, 20, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                5L, "Gasas", null, null, 1L, "COM-1");
        when(service.actualizar(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/lotes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_debeRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/lotes/1"))
                .andExpect(status().isOk());
    }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}
