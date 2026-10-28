package com.example.demo.modules.clinica.indicaciones.examenes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamenArchivoStorageTest {

    @TempDir
    private Path directorio;

    @Test
    void guardarY_cargar_debeConservarElArchivo() {
        ExamenArchivoStorage storage = new ExamenArchivoStorage(directorio);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "hemograma.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        String ruta = storage.guardar(archivo, 10L);

        assertTrue(ruta.startsWith("uploads/examenes/"));
        ExamenArchivoStorage.Archivo cargado = storage.cargar(ruta);
        assertEquals(MediaType.APPLICATION_PDF, cargado.tipo());
        assertTrue(cargado.recurso().exists());
    }

    @Test
    void guardarTxt_debeAceptarloYDevolverTextPlain() {
        ExamenArchivoStorage storage = new ExamenArchivoStorage(directorio);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "notas.txt", "text/plain", "contenido de texto".getBytes());

        String ruta = storage.guardar(archivo, 10L);

        assertEquals(MediaType.TEXT_PLAIN, storage.cargar(ruta).tipo());
    }

    @Test
    void guardar_debeRechazarContenidoQueNoCoincideConElTipo() {
        ExamenArchivoStorage storage = new ExamenArchivoStorage(directorio);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "hemograma.pdf", "application/pdf", "no es un pdf".getBytes());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> storage.guardar(archivo, 10L));

        assertEquals("El contenido no coincide con el tipo de archivo declarado", exception.getMessage());
    }

    @Test
    void eliminar_debeBorrarElArchivo() throws Exception {
        ExamenArchivoStorage storage = new ExamenArchivoStorage(directorio);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivos", "radiografia.png", "image/png", firmaPng());
        String ruta = storage.guardar(archivo, 10L);

        storage.eliminar(ruta);

        Path almacenado = directorio.resolve(Path.of(ruta).getFileName());
        assertFalse(Files.exists(almacenado));
    }

    private byte[] firmaPng() {
        return new byte[] {
                (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a,
                0x00, 0x00, 0x00, 0x0d
        };
    }
}
