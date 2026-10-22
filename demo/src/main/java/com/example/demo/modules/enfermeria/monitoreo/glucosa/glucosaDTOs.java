package com.example.demo.modules.enfermeria.monitoreo.glucosa;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class glucosaDTOs {

    public record Request(
            @NotNull(message = "La glucosa es obligatoria")
            @Positive(message = "La glucosa debe ser mayor a 0")
            Integer glucosa,
            @NotNull(message = "El tipo de medición es obligatorio")
            TipoMedicionGlucosa tipo,
            @Size(max = 2000, message = "La observación no debe superar los 2000 caracteres")
            String observacion,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            Integer glucosa,
            TipoMedicionGlucosa tipo,
            String observacion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean eliminado
    ) {}
}
