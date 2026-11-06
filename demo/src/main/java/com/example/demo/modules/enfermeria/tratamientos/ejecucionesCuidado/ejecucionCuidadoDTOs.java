package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ejecucionCuidadoDTOs {

    public record Request(
            @NotNull(message = "El cuidado es obligatorio")
            @Positive(message = "El cuidado debe ser un ID válido")
            Long cuidadoId,
            @NotNull(message = "La fecha y hora programada son obligatorias")
            LocalDateTime fechaHoraProgramada,
            @NotNull(message = "El estado es obligatorio")
            EstadoEjecucionCuidado estado,
            @Size(max = 2000, message = "La observación no debe superar los 2000 caracteres")
            String observacion,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            Long signosVitalesId,
            Long glucosaId,
            Long controlHidricoId
    ) {
        public Request(
                Long cuidadoId,
                LocalDateTime fechaHoraProgramada,
                EstadoEjecucionCuidado estado,
                String observacion,
                Long usuarioId) {
            this(cuidadoId, fechaHoraProgramada, estado, observacion, usuarioId, null, null, null);
        }
    }

    public record Response(
            Long id,
            Long cuidadoId,
            LocalDateTime fechaHoraProgramada,
            EstadoEjecucionCuidado estado,
            String observacion,
            Long usuarioId,
            String usuarioNombre,
            LocalDateTime fechaRegistro,
            Long signosVitalesId,
            Long glucosaId,
            Long controlHidricoId
    ) {
        public Response(
                Long id,
                Long cuidadoId,
                LocalDateTime fechaHoraProgramada,
                EstadoEjecucionCuidado estado,
                String observacion,
                Long usuarioId,
                String usuarioNombre,
                LocalDateTime fechaRegistro) {
            this(
                    id,
                    cuidadoId,
                    fechaHoraProgramada,
                    estado,
                    observacion,
                    usuarioId,
                    usuarioNombre,
                    fechaRegistro,
                    null,
                    null,
                    null);
        }
    }
}
