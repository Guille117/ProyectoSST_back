package com.example.demo.modules.bitacora;

import java.time.LocalDateTime;

public final class BitacoraDTOs {

    private BitacoraDTOs() {
    }

    public record BitacoraResponse(
            Long id,
            String tipo,
            String accion,
            String modulo,
            String entidad,
            Long entidadId,
            Long pacienteId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fechaHora,
            String detalle,
            String ip) {
    }
}
