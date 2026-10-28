package com.example.demo.modules.clinica.procedimientoMedico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class procedimientoMedicoDTOs {

    public record RecursoRequest(
            @NotNull(message = "El tipo de recurso es obligatorio")
            TipoRecursoProcedimiento tipo,
            @NotNull(message = "El recurso es obligatorio")
            @Positive(message = "El recurso debe ser un ID válido")
            Long referenciaId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad
    ) {}

    public record Request(
            @NotNull(message = "El tipo de procedimiento es obligatorio")
            @Positive(message = "El tipo de procedimiento debe ser un ID válido")
            Long tipoProcedimientoId,
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 2000, message = "La descripción no debe superar los 2000 caracteres")
            String descripcion,
            @Size(max = 2000, message = "El resultado no debe superar los 2000 caracteres")
            String resultado,
            @NotNull(message = "La fecha de inicio es obligatoria")
            LocalDateTime inicio,
            LocalDateTime fin,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            @NotNull(message = "Los recursos son obligatorios")
            @Size(max = 100, message = "El procedimiento no puede superar los 100 recursos")
            List<@Valid RecursoRequest> recursos
    ) {
        @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio")
        public boolean isFinValido() {
            return fin == null || inicio == null || !fin.isBefore(inicio);
        }
    }

    public record RecursoResponse(
            TipoRecursoProcedimiento tipo,
            Long referenciaId,
            String nombre,
            Integer cantidad
    ) {}

    public record Response(
            Long id,
            Long tipoProcedimientoId,
            String tipoProcedimientoNombre,
            String descripcion,
            String resultado,
            LocalDateTime inicio,
            LocalDateTime fin,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            EstadoProcedimientoMedico estado,
            List<RecursoResponse> recursos
    ) {}

    public record RecursoDisponibleResponse(
            Long id,
            String nombre,
            Integer disponible
    ) {}
}
