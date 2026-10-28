package com.example.demo.modules.clinica.indicaciones.examenes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(examenController.class)
class examenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @MockitoBean
    private examenService service;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_debeRetornarCreatedConLosArchivos() throws Exception {
        when(service.crear(any(), any())).thenReturn(new examenDTOs.Response(
                30L, "Hemograma", "En ayunas",
                List.of(new examenDTOs.ArchivoResponse(50L, "hemograma.pdf", "uploads/examenes/examen-30-a.pdf", 123L)),
                1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ACTIVO));

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(new examenDTOs.Request("Hemograma", "En ayunas", 1L, 2L)));
        MockMultipartFile archivo = new MockMultipartFile("archivos", "hemograma.pdf",
                "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        mockMvc.perform(multipart("/api/v1/examenes").file(requestPart).file(archivo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.nombreExamen").value("Hemograma"))
                .andExpect(jsonPath("$.archivos[0].id").value(50))
                .andExpect(jsonPath("$.archivos[0].urlArchivo").value("uploads/examenes/examen-30-a.pdf"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void agregarArchivos_debeRetornarElExamenActualizado() throws Exception {
        when(service.agregarArchivos(any(), any())).thenReturn(new examenDTOs.Response(
                30L, "Hemograma", "En ayunas",
                List.of(new examenDTOs.ArchivoResponse(60L, "hemograma.pdf", "uploads/examenes/examen-30-b.pdf", 123L)),
                1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ACTIVO));

        MockMultipartFile archivo = new MockMultipartFile("archivos", "hemograma.pdf",
                "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        mockMvc.perform(multipart("/api/v1/examenes/30/archivos").file(archivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivos[0].id").value(60))
                .andExpect(jsonPath("$.archivos[0].urlArchivo").value("uploads/examenes/examen-30-b.pdf"));
    }

    @Test
    void crear_debeRechazarRequestSinNombreDeExamen() throws Exception {
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                "{\"episodioId\":1,\"usuarioId\":2}".getBytes());

        mockMvc.perform(multipart("/api/v1/examenes").file(requestPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeAplicarEstadoActivoPorDefecto() throws Exception {
        when(service.obtenerTodos(EstadoExamen.ACTIVO)).thenReturn(List.of(new examenDTOs.Response(
                30L, "Hemograma", null, List.of(), 1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ACTIVO)));

        mockMvc.perform(get("/api/v1/examenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void buscar_debeFiltrarPorEpisodio() throws Exception {
        when(service.buscarPorEpisodio(5L, EstadoExamen.ACTIVO)).thenReturn(List.of(new examenDTOs.Response(
                31L, "Radiografía", null, List.of(), 5L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ACTIVO)));

        mockMvc.perform(get("/api/v1/examenes/buscar").param("episodioId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].episodioId").value(5));
    }

    @Test
    void obtenerPorId_debeRetornarElRegistro() throws Exception {
        when(service.obtenerPorId(30L)).thenReturn(new examenDTOs.Response(
                30L, "Hemograma", null,
                List.of(new examenDTOs.ArchivoResponse(50L, "hemograma.pdf", "uploads/examenes/examen-30-a.pdf", 123L)),
                1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ELIMINADO));

        mockMvc.perform(get("/api/v1/examenes/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.estado").value("ELIMINADO"));
    }

    @Test
    void actualizar_debeRetornarElRegistroActualizado() throws Exception {
        when(service.actualizar(any(), any(), any())).thenReturn(new examenDTOs.Response(
                30L, "Hemograma completo", "Revisar",
                List.of(new examenDTOs.ArchivoResponse(51L, "nuevo.pdf", "uploads/examenes/examen-30-nuevo.pdf", 456L)),
                1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ACTIVO));

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(new examenDTOs.Request("Hemograma completo", "Revisar", 1L, 2L)));
        MockMultipartFile archivo = new MockMultipartFile("archivos", "nuevo.pdf",
                "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        mockMvc.perform(multipart("/api/v1/examenes/30").file(requestPart).file(archivo)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreExamen").value("Hemograma completo"))
                .andExpect(jsonPath("$.archivos[0].urlArchivo").value("uploads/examenes/examen-30-nuevo.pdf"));
    }

    @Test
    void cambiarEstado_debeRetornarElNuevoEstado() throws Exception {
        when(service.cambiarEstado(30L, EstadoExamen.ELIMINADO)).thenReturn(new examenDTOs.Response(
                30L, "Hemograma", null, List.of(), 1L, 2L, "Carlos Mendez", FECHA, EstadoExamen.ELIMINADO));

        mockMvc.perform(patch("/api/v1/examenes/30/estado").param("estado", "ELIMINADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ELIMINADO"));
    }

    @Test
    void obtenerArchivo_debeRetornarElContenido() throws Exception {
        when(service.cargarArchivo(30L, 50L)).thenReturn(new ExamenArchivoStorage.Archivo(
                new ByteArrayResource("contenido".getBytes()), MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/v1/examenes/30/archivos/50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("contenido".getBytes()));
    }

    @Test
    void eliminarArchivo_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/examenes/30/archivos/50"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/examenes/30"))
                .andExpect(status().isNoContent());
    }
}
