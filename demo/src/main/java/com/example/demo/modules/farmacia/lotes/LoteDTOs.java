package com.example.demo.modules.farmacia.lotes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoteDTOs {

    public record Request(
            @NotNull(message = "El id de la compra es obligatorio") Long idCompra,
            Long idInsumoLog,
            Long idMedicamentoLog,
            @NotBlank(message = "El código del lote es obligatorio") String codigoLote,
            @NotNull(message = "La cantidad es obligatoria") @Positive(message = "La cantidad debe ser mayor a 0") Integer cantidad,
            // Opcional para insumos; para medicamentos es obligatoria (se valida en LoteService)
            LocalDate fechaVencimiento,
            @NotNull(message = "El precio de compra es obligatorio") @Positive(message = "El precio de compra debe ser mayor a 0") BigDecimal precioCompra,
            @NotNull(message = "El precio de venta es obligatorio") @Positive(message = "El precio de venta debe ser mayor a 0") BigDecimal precioVenta
    ) {}

    public record Response(
            Long id,
            String codigoLote,
            Integer cantidad,
            Integer disponible,
            boolean estado,
            LocalDate fechaVencimiento,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            BigDecimal total,
            Long idInsumoLog,
            String nombreInsumo,
            Long idMedicamentoLog,
            String nombreMedicamento,
            Long idCompra,
            String codigoCompra
    ) {}
}
