package com.example.demo.modules.farmacia.insumosLog;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(insumosLogController.class)
class insumosLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private insumosLogService service;

    @Test
    void obtenerTodos_debeFiltrarActivosPorDefectoYDevolverMarcaAnidada() throws Exception {
        when(service.obtenerTodos(true)).thenReturn(List.of(
                new insumosLogDTOs.ListadoResponse(10L, "Guantes",
                        new insumosLogDTOs.MarcaResponse("Marca A", 4L), true)));

        mockMvc.perform(get("/api/v1/insumosLog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Guantes"))
                .andExpect(jsonPath("$[0].marca.nombreMarca").value("Marca A"))
                .andExpect(jsonPath("$[0].marca.idMarca").value(4))
                .andExpect(jsonPath("$[0].estado").value(true));
    }

    @Test
    void buscar_debeAceptarNombreYMarcaId() throws Exception {
        when(service.buscar("guan", 4L, true)).thenReturn(List.of(
                new insumosLogDTOs.ListadoResponse(10L, "Guantes",
                        new insumosLogDTOs.MarcaResponse("Marca A", 4L), true)));

        mockMvc.perform(get("/api/v1/insumosLog/buscar")
                        .param("nombre", "guan")
                        .param("marcaId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca.idMarca").value(4));
    }

    @Test
    void cambiarEstado_debeRecibirIdEnLaRuta() throws Exception {
        mockMvc.perform(patch("/api/v1/insumosLog/10"))
                .andExpect(status().isOk());

        verify(service).cambiarEstado(10L);
    }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}