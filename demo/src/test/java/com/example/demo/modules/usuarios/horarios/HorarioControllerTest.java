package com.example.demo.modules.usuarios.horarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(HorarioController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private HorarioService service;

    @MockitoBean
    private HorarioMapper mapper;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_DeberiaRetornarCreated() throws Exception {
        HorarioDTOs.DetalleSemanalRequest detalle = new HorarioDTOs.DetalleSemanalRequest(
                DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true
        );
        HorarioDTOs.Request request = new HorarioDTOs.Request(
                "Horario Diurno", false, true, List.of(detalle), null
        );

        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Horario Diurno", false, true,
                List.of(new HorarioDTOs.DetalleSemanalResponse(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true)),
                null
        );

        when(service.crear(any(HorarioDTOs.Request.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("HOR-01"))
                .andExpect(jsonPath("$.nombre").value("Horario Diurno"));
    }

    @Test
    void crear_DeberiaRetornarBadRequest_CuandoNombreInvalido() throws Exception {
        HorarioDTOs.Request request = new HorarioDTOs.Request(
                "", false, true,
                List.of(new HorarioDTOs.DetalleSemanalRequest(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(17, 0), true)),
                null
        );

        mockMvc.perform(post("/api/v1/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_DeberiaRetornarOk() throws Exception {
        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Horario Diurno", false, true, List.of(), null
        );

        when(service.obtenerPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Horario Diurno"));
    }

    @Test
    void actualizar_DeberiaRetornarOk() throws Exception {
        HorarioDTOs.Request request = new HorarioDTOs.Request(
                "Horario Actualizado", false, true,
                List.of(new HorarioDTOs.DetalleSemanalRequest(DiaSemana.MARTES, LocalTime.of(9, 0), LocalTime.of(18, 0), true)),
                null
        );

        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Horario Actualizado", false, true, List.of(), null
        );

        when(service.actualizar(eq(1L), any(HorarioDTOs.Request.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/horarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Horario Actualizado"));
    }

    @Test
    void cambiarEstado_DeberiaRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/horarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        HorarioDTOs.Response response = new HorarioDTOs.Response(
                1L, "HOR-01", "Horario Diurno", false, true, List.of(), null
        );

        when(service.obtenerTodos(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("HOR-01"));
    }
}
