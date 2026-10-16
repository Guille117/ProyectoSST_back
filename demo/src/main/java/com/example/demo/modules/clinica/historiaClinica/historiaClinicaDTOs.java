package com.example.demo.modules.clinica.historiaClinica;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class historiaClinicaDTOs {

    public record Request(
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 2000, message = "La descripción no debe superar los 2000 caracteres")
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
            String descripcion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean estado
    ) {}
}
