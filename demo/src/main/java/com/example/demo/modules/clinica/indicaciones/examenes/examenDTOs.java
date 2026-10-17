package com.example.demo.modules.clinica.indicaciones.examenes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class examenDTOs {

    public record Request(
            @NotBlank(message = "El nombre del examen es obligatorio")
            @Size(max = 200, message = "El nombre del examen no debe superar los 200 caracteres")
            String nombreExamen,
            @Size(max = 2000, message = "La observación no debe superar los 2000 caracteres")
            String observacion,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record ArchivoResponse(
            Long id,
            String urlArchivo
    ) {}

    public record Response(
            Long id,
            String nombreExamen,
            String observacion,
            List<ArchivoResponse> archivos,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            EstadoExamen estado
    ) {}
}
