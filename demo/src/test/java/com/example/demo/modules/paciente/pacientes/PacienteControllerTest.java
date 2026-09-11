package com.example.demo.modules.paciente.pacientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PacienteService service;

    @Test
    void crear_debeRetornarCreatedCuandoValido() throws Exception {
        PacienteDTOs.Request req = new PacienteDTOs.Request("Test", true);
        PacienteDTOs.Response resp = new PacienteDTOs.Response(1L, "PAC-01", "Test", true);
        when(service.crear(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarBadRequestCuandoNombreVacio() throws Exception {
        String json = "{\"nombre\":\"\", \"estado\":true}";

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}
