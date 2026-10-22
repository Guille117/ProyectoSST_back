package com.example.demo.modules.enfermeria.notas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(notaEnfermeriaController.class)
class notaEnfermeriaControllerTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private notaEnfermeriaService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(service.crear(any())).thenReturn(new notaEnfermeriaDTOs.Response(
                10L, "Paciente estable", 1L, 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(post("/api/v1/enfermeria/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new notaEnfermeriaDTOs.Request(
                                "Paciente estable", 1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.texto").value("Paciente estable"));
    }

    @Test
    void crear_debeRechazarTextoVacio() throws Exception {
        mockMvc.perform(post("/api/v1/enfermeria/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"   \",\"episodioId\":1,\"usuarioId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_debeRetornarLasNotasDelEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L)).thenReturn(List.of(new notaEnfermeriaDTOs.Response(
                10L, "Paciente estable", 5L, 2L, "Carlos Mendez", FECHA, false)));

        mockMvc.perform(get("/api/v1/enfermeria/notas/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void actualizar_debeRetornarLaNotaActualizada() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new notaEnfermeriaDTOs.Response(
                10L, "Paciente con mejoría", 1L, 3L, "Ana Lopez", FECHA, false));

        mockMvc.perform(put("/api/v1/enfermeria/notas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new notaEnfermeriaDTOs.Request(
                                "Paciente con mejoría", 1L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Paciente con mejoría"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/enfermeria/notas/10"))
                .andExpect(status().isNoContent());
    }
}
