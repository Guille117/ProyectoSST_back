package com.example.demo.modules.pacientes.expediente;

import com.example.demo.modules.pacientes.paciente.TipoAtencion;

import java.time.LocalDateTime;

public final class ExpedienteDTOs {
    private ExpedienteDTOs() {
    }

    public record EpisodioResumenResponse(
            Long episodioId,
            TipoAtencion tipoAtencion,
            LocalDateTime fechaHoraIngreso,
            LocalDateTime fechaHoraEgreso,
            String medicoNombre,
            boolean activo
    ) {
    }
}
