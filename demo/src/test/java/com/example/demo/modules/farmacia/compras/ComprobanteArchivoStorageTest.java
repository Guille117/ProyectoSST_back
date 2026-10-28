package com.example.demo.modules.farmacia.compras;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ComprobanteArchivoStorageTest {

    @TempDir
    private Path directorio;

    @Test
    void constructorConBase_debeGuardarBajoComprobantes() {
        Path base = directorio.resolve("volumen");
        ComprobanteArchivoStorage storage = new ComprobanteArchivoStorage(base.toString());
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "comprobante.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());

        String ruta = storage.guardar(archivo, 5L);

        assertTrue(ruta.startsWith("uploads/comprobantes/"));
        assertTrue(Files.exists(base.resolve("comprobantes").resolve(Path.of(ruta).getFileName())));
    }
}
