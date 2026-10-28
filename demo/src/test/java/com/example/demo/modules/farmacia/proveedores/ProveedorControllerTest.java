package com.example.demo.modules.farmacia.proveedores;

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
@WebMvcTest(ProveedorController.class)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ProveedorService service;

    @Test
    void crear_debeAceptarNitConOchoDigitos() throws Exception {
        ProveedorDTOs.Request request = new ProveedorDTOs.Request(
                "Farmacia Central", "10503497-6", "12345678", "admin@farmacia.com", true);
        ProveedorDTOs.Response response = new ProveedorDTOs.Response(
                1L, "PROV-01", "Farmacia Central", "10503497-6", "12345678", "admin@farmacia.com", true);
        when(service.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/proveedores")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRechazarNitConMenosDeSieteDigitos() throws Exception {
        String json = "{\"nombre\":\"Farmacia Central\",\"nit\":\"123456-1\",\"telefono\":\"12345678\",\"email\":\"admin@farmacia.com\",\"estado\":true}";

        mockMvc.perform(post("/api/v1/proveedores")
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