package com.example.demo.modules.pacientes.institucion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(institucionController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class institucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private institucionService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_requiereNombreYPermiteDireccionTelefonoAusentes() throws Exception {
        when(service.guardar(any(institucionEntity.class))).thenAnswer(invocation -> {
            institucionEntity institucion = invocation.getArgument(0);
            institucion.setId(1L);
            return institucion;
        });

        mockMvc.perform(post("/api/v1/instituciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("nombre", "Hospital Central"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hospital Central"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void contarCatalogosActivos_devuelveElTotalDeInstituciones() throws Exception {
        when(service.contarActivas()).thenReturn(3L);

        mockMvc.perform(get("/api/v1/instituciones/conteo-catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tabla").value("instituciones"))
                .andExpect(jsonPath("$[0].total").value(3));
    }
}