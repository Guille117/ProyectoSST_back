package com.example.demo.modules.clinica.indicaciones.indicacionMedica;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class indicacionMedicaDTOs {

    public record Request(
            @NotNull(message = "El medicamento es obligatorio")
            @Positive(message = "El medicamento debe ser un ID válido")
            Long medicamentoLogId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @NotNull(message = "La frecuencia es obligatoria")
            @Positive(message = "La frecuencia debe ser mayor a 0")
            Integer frecuencia,
            Integer duracion,
            @NotNull(message = "El tipo de duración es obligatorio")
            TipoDuracion tipoDuracion,
            @Size(max = 2000, message = "Las observaciones no deben superar los 2000 caracteres")
            String observaciones,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            Long medicamentoLogId,
            String medicamento,
            Integer cantidad,
            Integer frecuencia,
            Integer duracion,
            TipoDuracion tipoDuracion,
            String observaciones,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            EstadoIndicacionMedica estado
    ) {}
}
