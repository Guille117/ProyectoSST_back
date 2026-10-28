package com.example.demo.modules.pacientes.paciente;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DocumentoEpisodioController.class)
class DocumentoEpisodioControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private DocumentoEpisodioService service;
    @MockitoBean private com.example.demo.security.JwtUtil jwtUtil;
    @MockitoBean private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    private DocumentoEpisodioDTOs.Response respuesta() {
        return new DocumentoEpisodioDTOs.Response(
                10L, TipoDocumentoEpisodio.REFERENCIA, "hoja.pdf",
                "uploads/documentos/episodio-8-REFERENCIA-uuid.pdf",
                LocalDateTime.of(2026, 10, 17, 10, 30), 3L, "Juan Pérez López", true);
    }

    @Test
    void listar_debeRetornarLosDocumentos() throws Exception {
        when(service.listar(8L, TipoDocumentoEpisodio.REFERENCIA, true))
                .thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/v1/episodios/8/documentos")
                        .param("tipo", "REFERENCIA")
                        .param("activos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].tipo").value("REFERENCIA"))
                .andExpect(jsonPath("$[0].nombreOriginal").value("hoja.pdf"))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Juan Pérez López"));
    }

    @Test
    void subir_debeRetornarCreated() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "hoja.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());
        when(service.subir(eq(8L), eq(TipoDocumentoEpisodio.REFERENCIA), eq(3L), any()))
                .thenReturn(List.of(respuesta()));

        mockMvc.perform(multipart("/api/v1/episodios/8/documentos")
                        .file(archivo)
                        .param("tipo", "REFERENCIA")
                        .param("usuarioId", "3"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nombreOriginal").value("hoja.pdf"));
    }

    @Test
    void cambiarEstado_debeRetornarOk() throws Exception {
        when(service.cambiarEstado(10L)).thenReturn(respuesta());

        mockMvc.perform(patch("/api/v1/documentos/10/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/documentos/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerArchivo_debeRetornarElContenido() throws Exception {
        when(service.cargarArchivo(10L)).thenReturn(new DocumentoEpisodioStorage.Archivo(
                new ByteArrayResource("%PDF-1.7 contenido".getBytes()), MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/v1/documentos/10/archivo"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_PDF));
    }
}
