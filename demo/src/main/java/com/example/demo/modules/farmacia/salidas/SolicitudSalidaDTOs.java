package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitudSalidaDTOs {

    /** Medicamento indicado en el alta que debe solicitarse a Farmacia. */
    public record LineaAlta(Long referenciaId, String nombre, Integer cantidad) {}

    /** Medicamento que Enfermería solicita a Farmacia. */
    public record MedicamentoSolicitudRequest(
            @NotNull(message = "El medicamento es obligatorio")
            @Positive(message = "El medicamento debe ser un ID válido")
            Long referenciaId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad
    ) {}

    /** Solicitud directa de medicamentos de Enfermería, sin procedimiento médico. */
    public record SolicitudMedicamentoRequest(
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            @NotEmpty(message = "Debe indicar al menos un medicamento")
            List<@Valid MedicamentoSolicitudRequest> medicamentos
    ) {}

    public record EntregaLineaRequest(
            @NotNull(message = "El detalle es obligatorio")
            @Positive(message = "El detalle debe ser un ID válido")
            Long detalleId,
            @NotNull(message = "El lote es obligatorio")
            @Positive(message = "El lote debe ser un ID válido")
            Long loteId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad
    ) {}

    public record EntregaRequest(
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            @NotEmpty(message = "Debe indicar al menos una línea para entregar")
            List<@Valid EntregaLineaRequest> lineas
    ) {}

    public record DevolucionLineaRequest(
            @NotNull(message = "El detalle es obligatorio")
            @Positive(message = "El detalle debe ser un ID válido")
            Long detalleId,
            @NotNull(message = "El lote es obligatorio")
            @Positive(message = "El lote debe ser un ID válido")
            Long loteId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @NotNull(message = "La condición es obligatoria")
            CondicionDevolucion condicion,
            @NotNull(message = "El motivo de devolución es obligatorio")
            @Positive(message = "El motivo de devolución debe ser un ID válido")
            Long motivoBajaId,
            @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
            String observacion
    ) {}

    public record DevolucionRequest(
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            @NotEmpty(message = "Debe indicar al menos una línea para devolver")
            List<@Valid DevolucionLineaRequest> lineas
    ) {}

    /** Línea de una salida directa: producto, lote elegido y cantidad que sale del inventario. */
    public record SalidaDirectaLineaRequest(
            @NotNull(message = "El tipo de recurso es obligatorio")
            TipoRecursoProcedimiento tipo,
            @NotNull(message = "El producto es obligatorio")
            @Positive(message = "El producto debe ser un ID válido")
            Long referenciaId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @NotNull(message = "El lote es obligatorio")
            @Positive(message = "El lote debe ser un ID válido")
            Long loteId
    ) {}

    /** Salida directa de Farmacia, ligada al episodio y sin procedimiento médico. */
    public record SalidaDirectaRequest(
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            @NotEmpty(message = "Debe indicar al menos un producto para la salida")
            List<@Valid SalidaDirectaLineaRequest> lineas
    ) {}

    /** Movimiento histórico de una salida: entrega o devolución agrupada por fecha y lote. */
    public record HistorialResponse(
            Long id,
            LocalDateTime fechaHora,
            TipoMovimientoSalida tipoMovimiento,
            TipoRecursoProcedimiento tipo,
            String producto,
            String lote,
            Integer cantidad,
            CondicionDevolucion condicion,
            String motivoBaja,
            String usuario,
            String observacion
    ) {}

    public record LoteDisponibleResponse(
            Long id,
            String codigoLote,
            LocalDate fechaVencimiento,
            Integer disponible
    ) {}

    public record LoteEntregadoResponse(
            Long loteId,
            String codigoLote,
            LocalDate fechaVencimiento,
            Integer entregado,
            Integer devuelto,
            Integer disponibleDevolver
    ) {}

    public record DetalleResponse(
            Long id,
            TipoRecursoProcedimiento tipo,
            Long referenciaId,
            String nombre,
            Integer solicitado,
            Integer entregado,
            Integer pendiente,
            EstadoDetalleSalida estado,
            List<LoteDisponibleResponse> lotesDisponibles,
            List<LoteEntregadoResponse> lotesEntregados
    ) {}

    public record ResumenResponse(
            Long id,
            String codigo,
            String paciente,
            String tipoAtencion,
            String procedimiento,
            LocalDateTime fecha,
            EstadoSolicitudSalida estado,
            Integer medicamentos,
            Integer insumos,
            Integer pendientes
    ) {}

    public record Response(
            Long id,
            String codigo,
            String paciente,
            String solicitante,
            String tipoAtencion,
            String procedimiento,
            LocalDateTime fecha,
            EstadoSolicitudSalida estado,
            List<DetalleResponse> detalles
    ) {}
}
