package com.example.demo.modules.enfermeria.notas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class notaEnfermeriaDTOs {

    public record Request(
            @NotBlank(message = "El texto de la nota es obligatorio")
            @Size(max = 4000, message = "La nota no debe superar los 4000 caracteres")
            String texto,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            String texto,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean eliminado
    ) {}
}
