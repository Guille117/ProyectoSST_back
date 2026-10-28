package com.example.demo.modules.clinica.condicionEgreso;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(condicionEgresoController.class)
class condicionEgresoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private condicionEgresoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_CreaCondicionDeEgresoConElCrudGenerico() throws Exception {
        when(service.guardar(any(condicionEgresoEntity.class))).thenAnswer(invocation -> {
            condicionEgresoEntity entidad = invocation.getArgument(0);
            entidad.setId(1L);
            return entidad;
        });

        mockMvc.perform(post("/api/v1/condicionesEgreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombre", "Curado"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Curado"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void listar_devuelveLasCondicionesDeEgresoActivas() throws Exception {
        condicionEgresoEntity condicion = new condicionEgresoEntity();
        condicion.setId(2L);
        condicion.setNombre("Mejorado");
        when(service.listarPorEstado(true)).thenReturn(List.of(condicion));

        mockMvc.perform(get("/api/v1/condicionesEgreso").param("activos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Mejorado"));
    }

    @Test
    void cambiarEstado_desactivaLaCondicionDeEgreso() throws Exception {
        condicionEgresoEntity condicion = new condicionEgresoEntity();
        condicion.setId(3L);
        condicion.setNombre("Fallecido");
        condicion.setEstado(false);
        when(service.cambiarEstado(3L)).thenReturn(Optional.of(condicion));

        mockMvc.perform(patch("/api/v1/condicionesEgreso/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.estado").value(false));
    }
}
