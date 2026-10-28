package com.example.demo.modules.clinica.parteCuerpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(parteCuerpoController.class)
class parteCuerpoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private parteCuerpoService service;

    @MockitoBean
    private com.example.demo.modules.clinica.condicionEgreso.condicionEgresoService condicionEgresoService;

    @MockitoBean
    private com.example.demo.modules.clinica.procedimiento.procedimientoService procedimientoService;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void contarCatalogos_devuelveLaCantidadDeLosCatalogosDeGestionClinica() throws Exception {
        when(service.contarPartesCuerpo()).thenReturn(5L);
        when(condicionEgresoService.contarCondicionesEgreso()).thenReturn(4L);
        when(procedimientoService.contarProcedimientos()).thenReturn(3L);

        mockMvc.perform(get("/api/v1/partesCuerpo/conteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partesCuerpo").value(5))
                .andExpect(jsonPath("$.condicionesEgreso").value(4))
                .andExpect(jsonPath("$.procedimientos").value(3));
    }

    @Test
    void crear_CreaParteDelCuerpoConElCrudGenerico() throws Exception {
        when(service.guardar(any(parteCuerpoEntity.class))).thenAnswer(invocation -> {
            parteCuerpoEntity entidad = invocation.getArgument(0);
            entidad.setId(1L);
            return entidad;
        });

        mockMvc.perform(post("/api/v1/partesCuerpo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombre", "Abdomen"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Abdomen"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void listar_devuelveLasPartesDelCuerpoActivas() throws Exception {
        parteCuerpoEntity parte = new parteCuerpoEntity();
        parte.setId(2L);
        parte.setNombre("Torax");
        when(service.listarPorEstado(true)).thenReturn(List.of(parte));

        mockMvc.perform(get("/api/v1/partesCuerpo").param("activos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Torax"));
    }
}
