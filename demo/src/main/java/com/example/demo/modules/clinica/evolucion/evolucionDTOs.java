package com.example.demo.modules.clinica.evolucion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class evolucionDTOs {

    public record Request(
            @NotBlank(message = "La evolución clínica es obligatoria")
            @Size(max = 1000, message = "La evolución clínica no debe superar los 1000 caracteres")
            String evolucionClinica,
            @NotBlank(message = "El plan médico es obligatorio")
            @Size(max = 1000, message = "El plan médico no debe superar los 1000 caracteres")
            String planMedico,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId
    ) {}

    public record Response(
            Long id,
            String evolucionClinica,
            String planMedico,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            EstadoEvolucion estado
    ) {}
}
