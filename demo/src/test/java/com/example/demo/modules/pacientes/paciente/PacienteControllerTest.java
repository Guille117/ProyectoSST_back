package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private PacienteService service;
    @MockitoBean private com.example.demo.security.JwtUtil jwtUtil;
    @MockitoBean private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crearDevuelveIdentificadoresDelExpedienteYEpisodio() throws Exception {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                "1234567890123", "Ana", "Lopez", LocalDate.of(1990, 1, 1), Sexo.FEMENINO,
                "12345678", null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(persona, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null));
        when(service.crear(any())).thenReturn(new PacienteDTOs.Response(1L, 2L, 3L, null, null, false));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.expedienteId").value(2))
                .andExpect(jsonPath("$.episodioId").value(3));
    }

    @Test
    void crearRechazaSolicitudSinDatosDePaciente() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content("{\"episodio\":{\"tipoAtencion\":\"EMERGENCIA\",\"descripcion\":\"Ingreso\",\"medicoId\":5}}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void crearPropagaComoBadRequestLaReglaDeResponsable() throws Exception {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                "1234567890123", "Ana", "Lopez", LocalDate.of(2015, 1, 1), Sexo.FEMENINO,
                null, null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(persona, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null));
        when(service.crear(any())).thenThrow(new IllegalArgumentException("El responsable es obligatorio"));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El responsable es obligatorio"));
    }
}