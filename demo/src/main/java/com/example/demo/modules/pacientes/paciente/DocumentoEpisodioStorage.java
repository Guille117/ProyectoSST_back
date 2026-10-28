package com.example.demo.modules.pacientes.paciente;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class DocumentoEpisodioStorage {

    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    public record Archivo(Resource recurso, MediaType tipo) {}

    private static final String RELATIVE_DIRECTORY = "uploads/documentos";
    private static final String LEGACY_DIRECTORY = "uploads/referencias";
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = new byte[] {
            (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a
    };

    private final Path directory;
    private final Path legacyDirectory;

    public DocumentoEpisodioStorage() {
        this(Path.of(RELATIVE_DIRECTORY), Path.of(LEGACY_DIRECTORY));
    }

    DocumentoEpisodioStorage(Path directory) {
        this(directory, directory);
    }

    DocumentoEpisodioStorage(Path directory, Path legacyDirectory) {
        this.directory = directory.toAbsolutePath().normalize();
        this.legacyDirectory = legacyDirectory.toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo, Long episodioId, TipoDocumentoEpisodio tipoDocumento) {
        if (archivo == null) {
            return null;
        }
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El documento está vacío");
        }
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El documento supera el máximo de 10 MB");
        }

        String extension = extensionPermitida(archivo.getContentType());
        try {
            byte[] contenido = archivo.getBytes();
            validarContenido(extension, contenido);

            String nombre = "episodio-" + episodioId + "-" + tipoDocumento.name() + "-" + UUID.randomUUID() + "." + extension;
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
            throw new IllegalStateException("No se pudo guardar el documento del episodio", ex);
        }
    }

    public Archivo cargar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            throw new IllegalArgumentException("El documento no tiene ruta");
        }
        Path destino = resolver(rutaRelativa);
        if (!Files.isRegularFile(destino)) {
            throw new IllegalArgumentException("El documento no existe");
        }
        return new Archivo(new FileSystemResource(destino), tipoContenido(destino));
    }

    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        Path destino = resolver(rutaRelativa);
        try {
            Files.deleteIfExists(destino);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo eliminar el documento del episodio", ex);
        }
    }

    private Path resolver(String rutaRelativa) {
        Path base;
        String prefijo;
        if (rutaRelativa.startsWith(RELATIVE_DIRECTORY + "/")) {
            base = directory;
            prefijo = RELATIVE_DIRECTORY + "/";
        } else if (rutaRelativa.startsWith(LEGACY_DIRECTORY + "/")) {
            base = legacyDirectory;
            prefijo = LEGACY_DIRECTORY + "/";
        } else {
            throw new IllegalArgumentException("Ruta de documento inválida");
        }
        Path destino = base.resolve(Path.of(rutaRelativa.substring(prefijo.length()))).normalize();
        if (!destino.startsWith(base)) {
            throw new IllegalArgumentException("Ruta de documento inválida");
        }
        return destino;
    }

    private MediaType tipoContenido(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (nombre.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String extensionPermitida(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("El tipo de archivo del documento no está permitido");
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
