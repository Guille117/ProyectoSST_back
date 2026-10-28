package com.example.demo.modules.clinica.diagnostico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class diagnosticoDTOs {

    public record Request(
            @NotNull(message = "El tipo de diagnóstico es obligatorio")
            TipoDiagnostico tipoDiagnostico,
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
            TipoDiagnostico tipoDiagnostico,
            String descripcion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean estado
    ) {}
}
