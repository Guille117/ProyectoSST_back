package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ejecucionCuidadoDTOs {

    public record Request(
            @NotNull(message = "El cuidado es obligatorio")
            @Positive(message = "El cuidado debe ser un ID válido")
            Long cuidadoId,
            @NotNull(message = "La fecha y hora programada son obligatorias")
            LocalDateTime fechaHoraProgramada,
            @NotNull(message = "El estado es obligatorio")
            EstadoEjecucionCuidado estado,
            @Size(max = 2000, message = "La observación no debe superar los 2000 caracteres")
            String observacion,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            Long cuidadoId,
            LocalDateTime fechaHoraProgramada,
            EstadoEjecucionCuidado estado,
            String observacion,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fechaRegistro
    ) {}
}
