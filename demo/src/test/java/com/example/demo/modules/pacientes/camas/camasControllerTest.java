package com.example.demo.modules.pacientes.camas;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}