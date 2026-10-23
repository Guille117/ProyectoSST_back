package com.example.demo.modules.pacientes.camas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public final class camasDTOs {
    private camasDTOs() {
    }

    public record Request(
            @NotNull(message = "La habitación es obligatoria") Long habitacionId,
            @NotNull(message = "El tipo de cama es obligatorio") Long tipoId,
            @NotNull(message = "El área es obligatoria") Long areaId,
            @PositiveOrZero(message = "El precio debe ser igual o mayor a 0") BigDecimal precio,
            EstadoCama estado,
            Boolean activo
    ) {
    }

    public record CatalogoResponse(Long id, String nombre) {
    }

    public record ReferenciasResponse(Long habitacionId, Long areaId, Long tipoId, BigDecimal precio) {
    }

    public record Response(Long id, String codigo, CatalogoResponse habitacion,
                           CatalogoResponse tipo, CatalogoResponse area,
                           EstadoCama estado, boolean activo, BigDecimal precio) {
    }

    public record ListResponse(Long idCama, String codigo, EstadoCama estado,
                               String nombreHabitacion, String nombreArea,
                               String nombreTipoCama, boolean activo, BigDecimal precio) {
    }

    public record OcupadaResponse(Long idCama, String codigo, String nombrePaciente,
                                  String nombreHabitacion, String nombreArea, String nombreTipoCama) {
    }


}