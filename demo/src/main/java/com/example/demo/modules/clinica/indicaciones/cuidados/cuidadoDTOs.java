package com.example.demo.modules.clinica.indicaciones.cuidados;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class cuidadoDTOs {

    public record Request(
            @NotBlank(message = "El nombre del cuidado es obligatorio")
            @Size(max = 200, message = "El nombre del cuidado no debe superar los 200 caracteres")
            String nombreCuidado,
            @NotNull(message = "La frecuencia es obligatoria")
            @Positive(message = "La frecuencia debe ser mayor a 0")
            Integer frecuencia,
            Integer duracion,
            @NotNull(message = "El tipo de duración es obligatorio")
            TipoDuracion tipoDuracion,
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
            String nombreCuidado,
            Integer frecuencia,
            Integer duracion,
            TipoDuracion tipoDuracion,
            String observacion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            EstadoCuidado estado
    ) {}
}
