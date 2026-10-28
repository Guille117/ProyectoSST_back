package com.example.demo.modules.farmacia.bajas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class BajaDTOs {

    public record Request(
            @NotNull(message = "El lote es obligatorio") @Positive(message = "El lote es inválido") Long idLote,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0") Integer cantidad,
            @NotNull(message = "El motivo de baja es obligatorio")
            @Positive(message = "El motivo de baja es inválido") Long idMotivoBaja,
            @Size(max = 500, message = "La observación no puede superar los 500 caracteres") String observacion,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario es inválido") Long idUsuario
    ) {}

    public record Response(
            Long id,
            Long idLote,
            String codigoLote,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockActual,
            Long idMotivoBaja,
            String motivo,
            String observacion,
            LocalDateTime fechaHora,
            Long idUsuario,
            String usuario
    ) {}

    public record ListadoResponse(
            Long idBaja,
            String producto,
            String lote,
            String motivo,
            String observacion,
            LocalDateTime fecha,
            String usuario
    ) {}
}
