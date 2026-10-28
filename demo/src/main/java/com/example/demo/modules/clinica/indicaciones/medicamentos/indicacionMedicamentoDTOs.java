package com.example.demo.modules.clinica.indicaciones.medicamentos;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class indicacionMedicamentoDTOs {

    public record Request(
            @Positive(message = "El medicamento debe ser un ID válido")
            Long medicamentoLogId,
            @Size(max = 1000, message = "El medicamento no registrado no debe superar los 1000 caracteres")
            String medicamentoLibre,
            @Size(max = 255, message = "La presentación no debe superar los 255 caracteres")
            String presentacion,
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
            Long usuarioId,
            @NotNull(message = "La fecha y hora de inicio son obligatorias")
            LocalDateTime fechaInicio
    ) {
        @AssertTrue(message = "El medicamento es obligatorio")
        public boolean isMedicamentoPresente() {
            return medicamentoLogId != null
                    || (medicamentoLibre != null && !medicamentoLibre.isBlank());
        }
    }

    public record Response(
            Long id,
            Long medicamentoLogId,
            String medicamento,
            String medicamentoLibre,
            String presentacion,
            Integer cantidad,
            Integer frecuencia,
            Integer duracion,
            TipoDuracion tipoDuracion,
            String observaciones,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            EstadoIndicacionMedicamento estado,
            LocalDateTime fechaInicio
    ) {}
}
