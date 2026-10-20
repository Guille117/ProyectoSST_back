package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SolicitudSalidaController.class)
class SolicitudSalidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SolicitudSalidaService service;

    @Test
    void listar_debeRetornarLasSolicitudes() throws Exception {
        when(service.listar(null, null)).thenReturn(List.of(resumen()));

        mockMvc.perform(get("/api/v1/solicitudesSalida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("SOL-00001"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void listar_debePasarLosFiltrosAlServicio() throws Exception {
        when(service.listar(EstadoSolicitudSalida.PARCIAL, "ana")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/solicitudesSalida")
                        .param("estado", "PARCIAL")
                        .param("texto", "ana"))
                .andExpect(status().isOk());

        verify(service).listar(EstadoSolicitudSalida.PARCIAL, "ana");
    }

    @Test
    void obtener_debeRetornarElDetalle() throws Exception {
        when(service.obtener(1L)).thenReturn(respuesta());

        mockMvc.perform(get("/api/v1/solicitudesSalida/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles[0].nombre").value("Amoxicilina"));
    }

    @Test
    void entregar_debeRetornarLaSolicitudActualizada() throws Exception {
        when(service.entregar(eq(1L), any())).thenReturn(respuesta());

        SolicitudSalidaDTOs.EntregaRequest request = new SolicitudSalidaDTOs.EntregaRequest(
                2L,
                List.of(new SolicitudSalidaDTOs.EntregaLineaRequest(10L, 20L, 3)));

        mockMvc.perform(post("/api/v1/solicitudesSalida/1/entregas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void entregar_sinLineasDebeRetornarBadRequest() throws Exception {
        SolicitudSalidaDTOs.EntregaRequest request = new SolicitudSalidaDTOs.EntregaRequest(2L, List.of());

        mockMvc.perform(post("/api/v1/solicitudesSalida/1/entregas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).entregar(any(), any());
    }

    @Test
    void devolver_debeRetornarLaSolicitudActualizada() throws Exception {
        when(service.devolver(eq(1L), any())).thenReturn(respuesta());

        SolicitudSalidaDTOs.DevolucionRequest request = new SolicitudSalidaDTOs.DevolucionRequest(
                2L,
                List.of(new SolicitudSalidaDTOs.DevolucionLineaRequest(
                        10L, 20L, 2, CondicionDevolucion.APTO, 7L, null)));

        mockMvc.perform(post("/api/v1/solicitudesSalida/1/devoluciones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void devolver_sinMotivoDebeRetornarBadRequest() throws Exception {
        String json = """
                {
                  "usuarioId": 2,
                  "lineas": [
                    {"detalleId": 10, "loteId": 20, "cantidad": 2, "condicion": "APTO"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/solicitudesSalida/1/devoluciones")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).devolver(any(), any());
    }

    private SolicitudSalidaDTOs.ResumenResponse resumen() {
        return new SolicitudSalidaDTOs.ResumenResponse(
                1L,
                "SOL-00001",
                "Ana López",
                "EMERGENCIA",
                "Sutura",
                LocalDateTime.of(2026, 10, 19, 10, 0),
                EstadoSolicitudSalida.PENDIENTE,
                1,
                0,
                1);
    }

    private SolicitudSalidaDTOs.Response respuesta() {
        return new SolicitudSalidaDTOs.Response(
                1L,
                "SOL-00001",
                "Ana López",
                "EMERGENCIA",
                "Sutura",
                LocalDateTime.of(2026, 10, 19, 10, 0),
                EstadoSolicitudSalida.PENDIENTE,
                List.of(new SolicitudSalidaDTOs.DetalleResponse(
                        10L,
                        TipoRecursoProcedimiento.MEDICAMENTO,
                        4L,
                        "Amoxicilina",
                        10,
                        0,
                        10,
                        EstadoDetalleSalida.PENDIENTE,
                        List.of(),
                        List.of())));
    }

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
}
