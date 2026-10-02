package com.example.demo.modules.pacientes.area;

import com.example.demo.modules.pacientes.habitaciones.habitacionesService;
import com.example.demo.modules.pacientes.institucion.institucionService;
import com.example.demo.modules.pacientes.parentesco.parentescoService;
import com.example.demo.modules.pacientes.tipoCama.tipoCamaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(areaController.class)
class areaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private areaService areaService;

    @MockitoBean
    private habitacionesService habitacionesService;

    @MockitoBean
    private institucionService institucionService;

    @MockitoBean
    private parentescoService parentescoService;

    @MockitoBean
    private tipoCamaService tipoCamaService;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void contarCatalogosActivos_debeIncluirInstitucionesYParentescos() throws Exception {
        when(institucionService.contarActivas()).thenReturn(4L);
        when(habitacionesService.contarActivos()).thenReturn(6L);
        when(tipoCamaService.contarActivos()).thenReturn(3L);
        when(parentescoService.contarActivos()).thenReturn(8L);

        mockMvc.perform(get("/api/v1/areas/conteo-catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(4)))
                .andExpect(jsonPath("$[0].tabla").value("instituciones"))
                .andExpect(jsonPath("$[0].total").value(4))
                .andExpect(jsonPath("$[1].tabla").value("habitaciones"))
                .andExpect(jsonPath("$[2].tabla").value("tipos_cama"))
                .andExpect(jsonPath("$[3].tabla").value("parentescos"))
                .andExpect(jsonPath("$[3].total").value(8));
    }
}