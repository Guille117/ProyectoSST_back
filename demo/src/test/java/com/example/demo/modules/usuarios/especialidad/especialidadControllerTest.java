package com.example.demo.modules.usuarios.especialidad;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(especialidadController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class especialidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private especialidadService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_CreaEspecialidadConElCrudGenerico() throws Exception {
        when(service.guardar(any(especialidadEntity.class))).thenAnswer(invocation -> {
            especialidadEntity especialidad = invocation.getArgument(0);
            especialidad.setId(1L);
            return especialidad;
        });

        mockMvc.perform(post("/api/v1/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("nombre", "Cardiología"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Cardiología"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void listar_DevuelveEspecialidades() throws Exception {
        especialidadEntity especialidad = new especialidadEntity();
        especialidad.setId(1L);
        especialidad.setNombre("Cardiología");
        when(service.listarTodos()).thenReturn(List.of(especialidad));

        mockMvc.perform(get("/api/v1/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cardiología"));
    }
}