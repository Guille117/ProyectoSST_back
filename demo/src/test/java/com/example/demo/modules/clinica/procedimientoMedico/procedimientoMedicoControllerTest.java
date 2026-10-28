package com.example.demo.modules.clinica.procedimientoMedico;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(procedimientoMedicoController.class)
class procedimientoMedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private procedimientoMedicoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_devuelveElProcedimientoRegistrado() throws Exception {
        when(service.crear(any(procedimientoMedicoDTOs.Request.class))).thenReturn(new procedimientoMedicoDTOs.Response(
                1L,
                2L,
                "Sutura",
                "Sutura de herida.",
                "Sin complicaciones.",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                LocalDateTime.of(2026, 10, 19, 12, 0),
                3L,
                4L,
                "Dra. Ana López",
                EstadoProcedimientoMedico.REALIZADO,
                List.of()));

        mockMvc.perform(post("/api/v1/procedimientosMedicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tipoProcedimientoId", 2,
                                "descripcion", "Sutura de herida.",
                                "resultado", "Sin complicaciones.",
                                "inicio", "2026-10-19T11:00",
                                "fin", "2026-10-19T12:00",
                                "episodioId", 3,
                                "usuarioId", 4,
                                "recursos", List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoProcedimientoNombre").value("Sutura"))
                .andExpect(jsonPath("$.estado").value("REALIZADO"));
    }

    @Test
    void buscar_porEpisodio_devuelveLosProcedimientos() throws Exception {
        when(service.buscarPorEpisodio(3L)).thenReturn(List.of(new procedimientoMedicoDTOs.Response(
                1L,
                2L,
                "Sutura",
                "Sutura de herida.",
                "",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                null,
                3L,
                4L,
                "Dra. Ana López",
                EstadoProcedimientoMedico.PENDIENTE,
                List.of())));

        mockMvc.perform(get("/api/v1/procedimientosMedicos/buscar").param("episodioId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void eliminar_marcaElProcedimientoComoEliminado() throws Exception {
        mockMvc.perform(delete("/api/v1/procedimientosMedicos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarRecursos_devuelveLasExistenciasDelTipoSolicitado() throws Exception {
        when(service.buscarRecursos(eq(TipoRecursoProcedimiento.MEDICAMENTO), eq("Amoxicilina")))
                .thenReturn(List.of(new procedimientoMedicoDTOs.RecursoDisponibleResponse(
                        4L, "Amoxicilina Bayer tabletas", 10)));

        mockMvc.perform(get("/api/v1/procedimientosMedicos/recursos")
                        .param("tipo", "MEDICAMENTO")
                        .param("nombre", "Amoxicilina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].disponible").value(10));
    }
}
