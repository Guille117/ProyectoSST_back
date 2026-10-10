package com.example.demo.modules.farmacia.compras;

import com.example.demo.modules.farmacia.lotes.LoteDTOs;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CompraDTOs {

    public record Request(
            Long proveedorId,
            @NotNull(message = "Debe enviar al menos un lote") List<LoteEntrada> lotes
    ) {}

    public record LoteEntrada(
            boolean esMedicamento,
            @NotNull(message = "El id del item del lote es obligatorio") Long idItem,
            @NotBlank(message = "El código del lote es obligatorio") String codigoLote,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0") Integer cantidad,
            // Opcional para insumos; para medicamentos es obligatoria (se valida en CompraService)
            LocalDate fechaVencimiento,
            @NotNull(message = "El precio de compra es obligatorio")
            @Positive(message = "El precio de compra debe ser mayor a 0") BigDecimal precioCompra,
            @NotNull(message = "El precio de venta es obligatorio")
            @Positive(message = "El precio de venta debe ser mayor a 0") BigDecimal precioVenta
    ) {}

    public record Response(
            Long id,
            String codigo,
            BigDecimal total,
            String comprobante,
            boolean estado,
            Long proveedorId,
            String proveedorNombre,
            LocalDateTime fecha,
            List<LoteDTOs.Response> lotes
    ) {}

    /**
     * Listado liviano para la consulta general de compras.
     * cantidadProductos = suma de la cantidad de todos los lotes de la compra (insumos y medicamentos).
     */
    public record ResumenResponse(
            Long id,
            String codigo,
            String proveedor,
            LocalDateTime fecha,
            Integer cantidadProductos,
            BigDecimal total
    ) {}

    /** Cabecera de la compra + detalle de cada lote con los datos de su medicamento o insumo. */
    public record DetalleResponse(
            Long id,
            String codigo,
            Long proveedorId,
            String proveedorNombre,
            LocalDateTime fecha,
            BigDecimal total,
            List<LoteDetalleResponse> lotes
    ) {}

    /**
     * Lote dentro del detalle de una compra.
     * - Si esMedicamento = true: nombre, dosis, unidadMedida (abreviatura), fabricante y presentacion.
     * - Si esMedicamento = false: nombre, fabricante (marca) y detalle; los campos exclusivos
     *   de medicamento (dosis, unidadMedida, presentacion) llegan en null.
     * codigo = código del medicamento o del insumo (no el código del lote, expuesto en codigoLote).
     */
    public record LoteDetalleResponse(
            Long id,
            String codigoLote,
            String codigo,
            boolean esMedicamento,
            String nombre,
            BigDecimal dosis,
            String unidadMedida,
            String fabricante,
            String presentacion,
            String detalle,
            Integer cantidad,
            BigDecimal precioCompra,
            LocalDate fechaVencimiento
    ) {}
}
