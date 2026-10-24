package com.example.demo.modules.pacientes.consumo;

import java.math.BigDecimal;
import java.util.List;

public final class ConsumoEpisodioDTOs {
    private ConsumoEpisodioDTOs() {
    }

    /** Línea de consumo: producto entregado o estancia en cama. */
    public record ItemResponse(
            String tipo,
            String nombre,
            String detalle,
            Integer cantidad,
            String unidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
    }

    public record Response(
            Long episodioId,
            List<ItemResponse> items,
            BigDecimal total
    ) {
    }
}
