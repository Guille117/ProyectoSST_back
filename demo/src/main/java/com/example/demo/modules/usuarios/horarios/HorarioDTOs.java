package com.example.demo.modules.usuarios.horarios;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public class HorarioDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,

            Boolean esRotativo,

            Boolean estado,

            @Valid List<DetalleSemanalRequest> semanalDetalles,

            @Valid TurnoDetalleRequest turnoDetalle
    ) {}

    public record DetalleSemanalRequest(
            @NotNull(message = "El día de semana es obligatorio") DiaSemana diaSemana,

            @NotNull(message = "La hora de entrada es obligatoria") LocalTime horaEntrada,

            @NotNull(message = "La hora de salida es obligatoria") LocalTime horaSalida,

            Boolean activo
    ) {}

    public record TurnoDetalleRequest(
            @NotNull(message = "Las horas de trabajo son obligatorias") Integer horasTrabajo,

            @NotNull(message = "Las horas de descanso son obligatorias") Integer horasDescanso
    ) {}

    public record Response(
            Long id,
            String codigo,
            String nombre,
            boolean esRotativo,
            boolean estado,
            List<DetalleSemanalResponse> semanalDetalles,
            TurnoDetalleResponse turnoDetalle
    ) {}

    public record DetalleSemanalResponse(
            Long id,
            DiaSemana diaSemana,
            LocalTime horaEntrada,
            LocalTime horaSalida,
            boolean activo
    ) {}

    public record TurnoDetalleResponse(
            Long id,
            Integer horasTrabajo,
            Integer horasDescanso
    ) {}
}
