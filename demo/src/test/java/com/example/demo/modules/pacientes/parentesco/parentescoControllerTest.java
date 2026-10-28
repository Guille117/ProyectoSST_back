package com.example.demo.modules.pacientes.parentesco;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(parentescoController.class)
class parentescoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private parentescoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeResponderCreated() throws Exception {
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Madre", true);
        when(service.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/parentescos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new parentescoDTOs.Request("Madre", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Madre"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void crear_debeRechazarNombreVacio() throws Exception {
        mockMvc.perform(post("/api/v1/parentescos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void obtenerPorId_debeResponderConParentesco() throws Exception {
        when(service.obtenerPorId(1L)).thenReturn(new parentescoDTOs.Response(1L, "Madre", true));

        mockMvc.perform(get("/api/v1/parentescos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Madre"));
    }

    @Test
    void actualizar_debeResponderConNombreActualizado() throws Exception {
        parentescoDTOs.Response response = new parentescoDTOs.Response(1L, "Tutor", true);
        when(service.actualizar(1L, new parentescoDTOs.Request("Tutor", null))).thenReturn(response);

        mockMvc.perform(put("/api/v1/parentescos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new parentescoDTOs.Request("Tutor", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tutor"));
    }

                @Test
                void cambiarEstado_debeResponderConEstadoActualizado() throws Exception {
                when(service.alternarEstado(1L)).thenReturn(java.util.Optional.of(
                    new parentescoDTOs.Response(1L, "Madre", false)));

                mockMvc.perform(patch("/api/v1/parentescos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value(false));
                }
}