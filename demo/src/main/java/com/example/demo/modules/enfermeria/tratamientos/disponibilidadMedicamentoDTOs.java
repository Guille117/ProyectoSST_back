package com.example.demo.modules.enfermeria.tratamientos;

/** Contratos de la disponibilidad en piso de medicamentos por episodio. */
public final class disponibilidadMedicamentoDTOs {
    private disponibilidadMedicamentoDTOs() {
    }

    /** Cantidad en piso (entregado menos consumido y devuelto) de un medicamento del episodio. */
    public record Response(
            Long medicamentoLogId,
            Integer disponible
    ) {
    }
}
