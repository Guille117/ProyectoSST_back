package com.example.demo.modules.farmacia.medicamentoLog;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class medicamentoLogDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotNull(message = "La dosis es obligatoria")
            @Positive(message = "La dosis debe ser un número mayor que cero") BigDecimal dosis,
            @NotNull(message = "La unidad de medida es obligatoria") Long unidadMedidaId,
            @NotNull(message = "La marca es obligatoria") Long marcaId,
            @NotNull(message = "La vía de administración es obligatoria") Long viaAdminId,
            @NotNull(message = "La presentación es obligatoria") Long presentacionId
    ) {}

    public record Response(
            Long id,
            String nombre,
            BigDecimal dosis,
            Long unidadMedidaId,
            String unidadMedida,
            Long marcaId,
            String marca,
            Long viaAdminId,
            String viaAdmin,
            Long presentacionId,
            String presentacion
    ) {}

    public record MedicamentoLogResponse(
            Long id,
            String nombre,
            BigDecimal dosis,
            String marca,
            String presentacion,
            String viaAdministracion,
            String unidadMedida,
            boolean estado
    ) {}

    public record MedicamentoLogBusquedaResponse(
            String nombre,
            BigDecimal dosis,
            Long unidadMedidaId,
            Long viaAdminId,
            Long presentacionId,
            Long marcaId
    ) {}
}