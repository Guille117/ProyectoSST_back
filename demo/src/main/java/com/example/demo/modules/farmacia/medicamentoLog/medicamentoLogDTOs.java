package com.example.demo.modules.farmacia.medicamentoLog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class medicamentoLogDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "La dosis es obligatoria") String dosis,
            @NotNull(message = "La unidad de medida es obligatoria") Long unidadMedidaId,
            @NotNull(message = "La marca es obligatoria") Long marcaId,
            @NotNull(message = "La vía de administración es obligatoria") Long viaAdminId,
            @NotNull(message = "La presentación es obligatoria") Long presentacionId
    ) {}

    public record Response(
            Long id,
            String nombre,
            String dosis,
            Long unidadMedidaId,
            String unidadMedida,
            Long marcaId,
            String marca,
            Long viaAdminId,
            String viaAdmin,
            Long presentacionId,
            String presentacion
    ) {}
}