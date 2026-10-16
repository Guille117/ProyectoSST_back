package com.example.demo.modules.clinica.examenFisico;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(examenFisicoController.class)
class examenFisicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 10, 15);

    @MockitoBean
    private examenFisicoService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConElRegistroGuardado() throws Exception {
        when(service.crear(any())).thenReturn(new examenFisicoDTOs.Response(
                20L, "Ruidos cardiacos normales", 1L, 3L, "Torax", 2L, "Carlos Mendez", FECHA, true));

        mockMvc.perform(post("/api/v1/examenFisico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new examenFisicoDTOs.Request("Ruidos cardiacos normales", 1L, 3L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.descripcion").value("Ruidos cardiacos normales"))
                .andExpect(jsonPath("$.episodioId").value(1))
                .andExpect(jsonPath("$.parteCuerpoId").value(3))
                .andExpect(jsonPath("$.parteCuerpoNombre").value("Torax"))
                .andExpect(jsonPath("$.usuarioNombre").value("Carlos Mendez"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void crear_debeRechazarRequestSinParteDelCuerpo() throws Exception {
        String json = "{\"descripcion\":\"Inspección\",\"episodioId\":1,\"usuarioId\":2}";

        mockMvc.perform(post("/api/v1/examenFisico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarActivoTruePorDefecto() throws Exception {
        when(service.obtenerTodos(true)).thenReturn(List.of(
                new examenFisicoDTOs.Response(20L, "Ruidos cardiacos normales", 1L, 3L, "Torax", 2L,
                        "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/examenFisico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].parteCuerpoNombre").value("Torax"))
                .andExpect(jsonPath("$[0].estado").value(true));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, true)).thenReturn(List.of(
                new examenFisicoDTOs.Response(21L, "Inspección general", 5L, 3L, "Torax", 2L,
                        "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/examenFisico/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(21))
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void buscar_debeFiltrarPorParteDelCuerpo() throws Exception {
        when(service.buscarPorParteCuerpo(4L, true)).thenReturn(List.of(
                new examenFisicoDTOs.Response(22L, "Palpación", 1L, 4L, "Abdomen", 2L,
                        "Carlos Mendez", FECHA, true)));

        mockMvc.perform(get("/api/v1/examenFisico/buscar").param("parteCuerpoId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(22))
                .andExpect(jsonPath("$[0].parteCuerpoId").value(4));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(20L)).thenReturn(
                new examenFisicoDTOs.Response(20L, "Inspección", 1L, 3L, "Torax", 2L,
                        "Carlos Mendez", FECHA, false));

        mockMvc.perform(get("/api/v1/examenFisico/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any())).thenReturn(new examenFisicoDTOs.Response(
                20L, "Dolor a la palpación", 1L, 4L, "Abdomen", 2L, "Carlos Mendez", FECHA, true));

        mockMvc.perform(put("/api/v1/examenFisico/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new examenFisicoDTOs.Request("Dolor a la palpación", 1L, 4L, 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Dolor a la palpación"))
                .andExpect(jsonPath("$.parteCuerpoNombre").value("Abdomen"));
    }

    @Test
    void cambiarEstado_debeRetornarElEstadoAlternado() throws Exception {
        when(service.cambiarEstado(20L)).thenReturn(new examenFisicoDTOs.Response(
                20L, "Ruidos cardiacos normales", 1L, 3L, "Torax", 2L, "Carlos Mendez", FECHA, false));

        mockMvc.perform(patch("/api/v1/examenFisico/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.estado").value(false));
    }
}

