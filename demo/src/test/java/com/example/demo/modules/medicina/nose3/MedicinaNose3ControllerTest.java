package com.example.demo.modules.medicina.nose3;

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
@WebMvcTest(MedicinaNose3Controller.class)
class MedicinaNose3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicinaNose3Service service;

    @Test
    void crear_debeRetornarCreatedCuandoValido() throws Exception {
        MedicinaNose3DTOs.Request req = new MedicinaNose3DTOs.Request("Test", true);
        MedicinaNose3DTOs.Response resp = new MedicinaNose3DTOs.Response(1L, "MED3-01", "Test", true);
        when(service.crear(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/medicina-nose3")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarBadRequestCuandoNombreVacio() throws Exception {
        String json = "{\"nombre\":\"\", \"estado\":true}";

        mockMvc.perform(post("/api/v1/medicina-nose3")
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
