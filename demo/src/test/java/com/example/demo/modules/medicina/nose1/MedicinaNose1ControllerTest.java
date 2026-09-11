package com.example.demo.modules.medicina.nose1;

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
@WebMvcTest(MedicinaNose1Controller.class)
class MedicinaNose1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicinaNose1Service service;

    @Test
    void crear_debeRetornarCreatedCuandoValido() throws Exception {
        MedicinaNose1DTOs.Request req = new MedicinaNose1DTOs.Request("Test", true);
        MedicinaNose1DTOs.Response resp = new MedicinaNose1DTOs.Response(1L, "MED1-01", "Test", true);
        when(service.crear(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/medicina-nose1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarBadRequestCuandoNombreVacio() throws Exception {
        String json = "{\"nombre\":\"\", \"estado\":true}";

        mockMvc.perform(post("/api/v1/medicina-nose1")
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
