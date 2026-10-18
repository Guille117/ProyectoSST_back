package com.example.demo.modules.pacientes.paciente;

import java.time.LocalDateTime;

public class DocumentoEpisodioDTOs {

    public record Response(
            Long id,
            TipoDocumentoEpisodio tipo,
            String nombreOriginal,
            String urlArchivo,
            LocalDateTime fechaSubida,
            Long usuarioId,
            String usuarioNombre,
            boolean estado
    ) {}
}
