package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtil;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SalidaDirectaController.class)
class SalidaDirectaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SolicitudSalidaService service;

    @Test
    void crear_debeRegistrarLaSalidaDirecta() throws Exception {
        when(service.crearDirecta(any())).thenReturn(respuesta());

        SolicitudSalidaDTOs.SalidaDirectaRequest request =
                new SolicitudSalidaDTOs.SalidaDirectaRequest(
                        10L,
                        1L,
                        List.of(new SolicitudSalidaDTOs.SalidaDirectaLineaRequest(
                                TipoRecursoProcedimiento.MEDICAMENTO, 4L, 3, 20L)));

        mockMvc.perform(post("/api/v1/salidasDirectas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("SAL-M00001"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void crear_sinLineasDebeRetornarBadRequest() throws Exception {
        SolicitudSalidaDTOs.SalidaDirectaRequest request =
                new SolicitudSalidaDTOs.SalidaDirectaRequest(10L, 1L, List.of());

        mockMvc.perform(post("/api/v1/salidasDirectas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private SolicitudSalidaDTOs.Response respuesta() {
        return new SolicitudSalidaDTOs.Response(
                1L,
                "SAL-M00001",
                "Ana López",
                "Dra. María Pérez",
                "EMERGENCIA",
                "",
                LocalDateTime.of(2026, 10, 19, 10, 0),
                EstadoSolicitudSalida.COMPLETADA,
                List.of());
    }

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
}
