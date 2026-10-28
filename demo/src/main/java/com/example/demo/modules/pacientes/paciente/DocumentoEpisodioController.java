package com.example.demo.modules.pacientes.paciente;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentoEpisodioController {

    private final DocumentoEpisodioService service;

    @GetMapping("/episodios/{episodioId}/documentos")
    public ResponseEntity<List<DocumentoEpisodioDTOs.Response>> listar(
            @PathVariable("episodioId") Long episodioId,
            @RequestParam(name = "tipo", required = false) TipoDocumentoEpisodio tipo,
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.listar(episodioId, tipo, activos));
    }

    @PostMapping(value = "/episodios/{episodioId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentoEpisodioDTOs.Response>> subir(
            @PathVariable("episodioId") Long episodioId,
            @RequestParam(name = "tipo") TipoDocumentoEpisodio tipo,
            @RequestParam(name = "usuarioId") Long usuarioId,
            @RequestPart("archivos") List<MultipartFile> archivos) {
        return new ResponseEntity<>(service.subir(episodioId, tipo, usuarioId, archivos), HttpStatus.CREATED);
    }

    @PatchMapping("/documentos/{id}/estado")
    public ResponseEntity<DocumentoEpisodioDTOs.Response> cambiarEstado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }

    @DeleteMapping("/documentos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documentos/{id}/archivo")
    public ResponseEntity<Resource> obtenerArchivo(@PathVariable("id") Long id) {
        DocumentoEpisodioStorage.Archivo archivo = service.cargarArchivo(id);
        return ResponseEntity.ok()
                .contentType(archivo.tipo())
                .body(archivo.recurso());
    }
}
