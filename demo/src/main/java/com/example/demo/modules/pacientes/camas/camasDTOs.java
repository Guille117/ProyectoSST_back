package com.example.demo.modules.pacientes.camas;

import jakarta.validation.constraints.NotNull;

public final class camasDTOs {
    private camasDTOs() {
    }

    public record Request(
            @NotNull(message = "La habitación es obligatoria") Long habitacionId,
            @NotNull(message = "El tipo de cama es obligatorio") Long tipoId,
            @NotNull(message = "El área es obligatoria") Long areaId,
            EstadoCama estado,
            Boolean activo
    ) {
    }

    public record CatalogoResponse(Long id, String nombre) {
    }

    public record ReferenciasResponse(Long habitacionId, Long areaId, Long tipoId) {
    }

    public record Response(Long id, String codigo, CatalogoResponse habitacion,
                           CatalogoResponse tipo, CatalogoResponse area,
                           EstadoCama estado, boolean activo) {
    }

    public record ListResponse(Long idCama, String codigo, EstadoCama estado,
                               String nombreHabitacion, String nombreArea,
                               String nombreTipoCama, boolean activo) {
    }


}