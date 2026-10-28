package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class aplicacionMedicamentoDTOs {

    public record Request(
            @NotNull(message = "La indicación es obligatoria")
            @Positive(message = "La indicación debe ser un ID válido")
            Long indicacionMedicamentoId,
            @NotNull(message = "La fecha y hora programada son obligatorias")
            LocalDateTime fechaHoraProgramada,
            @NotNull(message = "El estado es obligatorio")
            EstadoAplicacionMedicamento estado,
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @Size(max = 2000, message = "La observación no debe superar los 2000 caracteres")
            String observacion,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            Long indicacionMedicamentoId,
            LocalDateTime fechaHoraProgramada,
            EstadoAplicacionMedicamento estado,
            Integer cantidad,
            String observacion,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fechaRegistro
    ) {}
}
