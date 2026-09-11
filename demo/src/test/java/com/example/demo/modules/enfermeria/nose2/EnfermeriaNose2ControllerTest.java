package com.example.demo.modules.enfermeria.nose2;

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
@WebMvcTest(EnfermeriaNose2Controller.class)
class EnfermeriaNose2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnfermeriaNose2Service service;

    @Test
    void crear_debeRetornarCreatedCuandoValido() throws Exception {
        EnfermeriaNose2DTOs.Request req = new EnfermeriaNose2DTOs.Request("Test", true);
        EnfermeriaNose2DTOs.Response resp = new EnfermeriaNose2DTOs.Response(1L, "ENF2-01", "Test", true);
        when(service.crear(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/enfermeria-nose2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarBadRequestCuandoNombreVacio() throws Exception {
        String json = "{\"nombre\":\"\", \"estado\":true}";

        mockMvc.perform(post("/api/v1/enfermeria-nose2")
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
