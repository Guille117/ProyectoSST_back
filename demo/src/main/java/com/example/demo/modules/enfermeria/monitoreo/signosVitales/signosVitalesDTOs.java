package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class signosVitalesDTOs {

    public record Request(
            @NotNull(message = "La presión sistólica es obligatoria")
            @Positive(message = "La presión sistólica debe ser mayor a 0")
            Integer sistolica,
            @NotNull(message = "La presión diastólica es obligatoria")
            @Positive(message = "La presión diastólica debe ser mayor a 0")
            Integer diastolica,
            @NotNull(message = "La frecuencia cardíaca es obligatoria")
            @Positive(message = "La frecuencia cardíaca debe ser mayor a 0")
            Integer frecuenciaCardiaca,
            @NotNull(message = "La frecuencia respiratoria es obligatoria")
            @Positive(message = "La frecuencia respiratoria debe ser mayor a 0")
            Integer frecuenciaRespiratoria,
            @NotNull(message = "La temperatura es obligatoria")
            @Positive(message = "La temperatura debe ser mayor a 0")
            BigDecimal temperatura,
            @NotNull(message = "La saturación es obligatoria")
            @Min(value = 0, message = "La saturación no puede ser negativa")
            @Max(value = 100, message = "La saturación no puede superar 100")
            Integer saturacion,
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
            Integer sistolica,
            Integer diastolica,
            Integer frecuenciaCardiaca,
            Integer frecuenciaRespiratoria,
            BigDecimal temperatura,
            Integer saturacion,
            String observacion,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fecha,
            boolean eliminado
    ) {}
}
