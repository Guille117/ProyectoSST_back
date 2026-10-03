package com.example.demo.modules.pacientes.paciente;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReferenciaArchivoStorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void guardar_debeUsarIdDeExpedienteYNombreGenerado() throws Exception {
        ReferenciaArchivoStorage storage = new ReferenciaArchivoStorage(temporaryDirectory);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivoReferencia", "../../original.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        String ruta = storage.guardar(archivo, 30L);

        assertTrue(ruta.startsWith("uploads/referencias/expediente-30-"));
        assertTrue(ruta.endsWith(".pdf"));
        Path archivoGuardado = temporaryDirectory.resolve(Path.of(ruta).getFileName());
        assertTrue(Files.exists(archivoGuardado));

        storage.eliminar(ruta);
        assertFalse(Files.exists(archivoGuardado));
    }

    @Test
    void guardar_debeRechazarContenidoQueNoCoincideConElTipo() {
        ReferenciaArchivoStorage storage = new ReferenciaArchivoStorage(temporaryDirectory);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivoReferencia", "falso.pdf", "application/pdf", "no es pdf".getBytes());

        assertThrows(IllegalArgumentException.class, () -> storage.guardar(archivo, 30L));
    }

    @Test
    void guardar_debeRechazarArchivosMayoresA10Mb() {
        ReferenciaArchivoStorage storage = new ReferenciaArchivoStorage(temporaryDirectory);
        MultipartFile archivo = mock(MultipartFile.class);
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getSize()).thenReturn(ReferenciaArchivoStorage.MAX_FILE_SIZE + 1);

        assertThrows(IllegalArgumentException.class, () -> storage.guardar(archivo, 30L));
    }
}