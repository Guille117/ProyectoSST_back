package com.example.demo.modules.pacientes.camas;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(camasController.class)
class camasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private camasService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void obtenerOcupadas_devuelveNombreCompletoDelPaciente() throws Exception {
        when(service.obtenerOcupadas()).thenReturn(List.of(new camasDTOs.OcupadaResponse(
                9L, "CAMA-09", "Lesly Maria Herrera", "Habitación 1", "Emergencia", "Individual")));

        mockMvc.perform(get("/api/v1/camas/ocupadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCama").value(9))
                .andExpect(jsonPath("$[0].codigo").value("CAMA-09"))
                .andExpect(jsonPath("$[0].nombrePaciente").value("Lesly Maria Herrera"));
    }

    @Test
    void crear_rechazaPrecioNegativo() throws Exception {
        mockMvc.perform(post("/api/v1/camas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "habitacionId": 1,
                                    "tipoId": 1,
                                    "areaId": 1,
                                    "precio": -0.01
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(service, never()).crear(any(camasDTOs.Request.class));
    }

    @Test
    void crear_aceptaPrecioCeroYDevuelvePrecio() throws Exception {
        when(service.crear(any(camasDTOs.Request.class))).thenReturn(new camasDTOs.Response(
                1L, "CAMA-01",
                new camasDTOs.CatalogoResponse(1L, "Habitación 1"),
                new camasDTOs.CatalogoResponse(1L, "Individual"),
                new camasDTOs.CatalogoResponse(1L, "Emergencia"),
                EstadoCama.DISPONIBLE, true, BigDecimal.ZERO));

        mockMvc.perform(post("/api/v1/camas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "habitacionId": 1,
                                    "tipoId": 1,
                                    "areaId": 1,
                                    "precio": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio").value(0));

        ArgumentCaptor<camasDTOs.Request> captor = ArgumentCaptor.forClass(camasDTOs.Request.class);
        verify(service).crear(captor.capture());
        assertEquals(0, captor.getValue().precio().compareTo(BigDecimal.ZERO));
    }

    @Test
    void obtenerTodos_incluyePrecio() throws Exception {
        BigDecimal precio = new BigDecimal("25.50");
        when(service.obtenerTodos(null)).thenReturn(List.of(new camasDTOs.ListResponse(
                1L, "CAMA-01", EstadoCama.DISPONIBLE, "Habitación 1", "Emergencia", "Individual",
                true, precio)));

        mockMvc.perform(get("/api/v1/camas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precio").value(25.50));
    }
}