package com.example.demo.modules.pacientes.paciente;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoEpisodioStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void guardar_debeCrearElArchivoYRetornarLaRuta() throws Exception {
        DocumentoEpisodioStorage storage = new DocumentoEpisodioStorage(tempDir.resolve("documentos"));
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "hoja-referencia.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        String ruta = storage.guardar(archivo, 8L, TipoDocumentoEpisodio.REFERENCIA);

        assertThat(ruta).startsWith("uploads/documentos/episodio-8-REFERENCIA-").endsWith(".pdf");
        assertThat(Files.exists(tempDir.resolve("documentos").resolve(Path.of(ruta).getFileName()))).isTrue();
    }

    @Test
    void guardar_debeRechazarArchivoVacioOTipoNoPermitido() {
        DocumentoEpisodioStorage storage = new DocumentoEpisodioStorage(tempDir.resolve("documentos"));
        MockMultipartFile vacio = new MockMultipartFile("archivos", "vacio.pdf", "application/pdf", new byte[0]);
        MockMultipartFile word = new MockMultipartFile(
                "archivos", "documento.docx", "application/msword", "contenido".getBytes());

        assertThatThrownBy(() -> storage.guardar(vacio, 8L, TipoDocumentoEpisodio.REFERENCIA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vacío");
        assertThatThrownBy(() -> storage.guardar(word, 8L, TipoDocumentoEpisodio.REFERENCIA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo se permiten");
    }

    @Test
    void cargarYeliminar_debenResolverLaRutaNueva() throws Exception {
        DocumentoEpisodioStorage storage = new DocumentoEpisodioStorage(tempDir.resolve("documentos"));
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "autorizacion.png", "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1});
        String ruta = storage.guardar(archivo, 9L, TipoDocumentoEpisodio.AUTORIZACION_PROCEDIMIENTO);

        DocumentoEpisodioStorage.Archivo cargado = storage.cargar(ruta);

        assertThat(cargado.tipo()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(cargado.recurso().exists()).isTrue();
        storage.eliminar(ruta);
        assertThatThrownBy(() -> storage.cargar(ruta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void cargar_debePermitirRutasLegacyDeReferencias() throws Exception {
        Path documentos = tempDir.resolve("documentos");
        Path referencias = tempDir.resolve("referencias");
        Files.createDirectories(referencias);
        Files.write(referencias.resolve("legacy.pdf"), "%PDF-1.7".getBytes());
        DocumentoEpisodioStorage storage = new DocumentoEpisodioStorage(documentos, referencias);

        DocumentoEpisodioStorage.Archivo cargado = storage.cargar("uploads/referencias/legacy.pdf");

        assertThat(cargado.tipo()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(cargado.recurso().exists()).isTrue();
    }

    @Test
    void cargar_debeRechazarRutasFueraDeLasCarpetas() {
        DocumentoEpisodioStorage storage = new DocumentoEpisodioStorage(tempDir.resolve("documentos"));

        assertThatThrownBy(() -> storage.cargar("uploads/documentos/../secreto.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválida");
        assertThatThrownBy(() -> storage.cargar("otra/ruta.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválida");
    }
}
