package com.example.demo.modules.farmacia.compras;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ComprobanteArchivoStorage {

    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String RELATIVE_DIRECTORY = "uploads/comprobantes";
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = new byte[] {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    private final Path directory;

    public ComprobanteArchivoStorage() {
        this(Path.of(RELATIVE_DIRECTORY));
    }

    ComprobanteArchivoStorage(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo, Long compraId) {
        if (archivo == null) {
            return null;
        }
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El comprobante está vacío");
        }
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El comprobante supera el máximo de 10 MB");
        }

        String extension = extensionPermitida(archivo.getContentType());
        try {
            byte[] contenido = archivo.getBytes();
            validarContenido(extension, contenido);

            String nombre = "compra-" + compraId + "-" + UUID.randomUUID() + "." + extension;
            Files.createDirectories(directory);
            Path destino = directory.resolve(nombre).normalize();
            if (!destino.startsWith(directory)) {
                throw new IllegalArgumentException("Nombre de archivo inválido");
            }

            try {
                Files.write(destino, contenido, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (IOException ex) {
                Files.deleteIfExists(destino);
                throw ex;
            }
            return RELATIVE_DIRECTORY + "/" + nombre;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el comprobante", ex);
        }
    }

    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        if (!rutaRelativa.startsWith(RELATIVE_DIRECTORY + "/")) {
            throw new IllegalArgumentException("Ruta de comprobante inválida");
        }

        Path destino = directory.resolve(Path.of(rutaRelativa).getFileName()).normalize();
        if (!destino.startsWith(directory)) {
            throw new IllegalArgumentException("Ruta de comprobante inválida");
        }
        try {
            Files.deleteIfExists(destino);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo eliminar el comprobante", ex);
        }
    }

    private String extensionPermitida(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("El tipo de archivo del comprobante no está permitido");
        }
        return switch (contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim()) {
            case "application/pdf" -> "pdf";
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new IllegalArgumentException("Solo se permiten archivos PDF, JPG, JPEG o PNG");
        };
    }

    private void validarContenido(String extension, byte[] contenido) {
        boolean valido = switch (extension) {
            case "pdf" -> empiezaCon(contenido, PDF_SIGNATURE);
            case "jpg" -> contenido.length >= 3
                    && (contenido[0] & 0xff) == 0xff
                    && (contenido[1] & 0xff) == 0xd8
                    && (contenido[2] & 0xff) == 0xff;
            case "png" -> empiezaCon(contenido, PNG_SIGNATURE);
            default -> false;
        };
        if (!valido) {
            throw new IllegalArgumentException("El contenido no coincide con el tipo de archivo declarado");
        }
    }

    private boolean empiezaCon(byte[] contenido, byte[] firma) {
        if (contenido.length < firma.length) {
            return false;
        }
        for (int indice = 0; indice < firma.length; indice++) {
            if (contenido[indice] != firma[indice]) {
                return false;
            }
        }
        return true;
    }
}
