package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class controlHidricoDTOs {

    public record Request(
            @NotNull(message = "El tipo de registro es obligatorio")
            TipoRegistroHidrico tipo,
            @NotBlank(message = "La vía es obligatoria")
            @Size(max = 100, message = "La vía no debe superar los 100 caracteres")
            String via,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
            String descripcion,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            TipoRegistroHidrico tipo,
            String via,
            Integer cantidad,
            String descripcion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean eliminado
    ) {}
}
