package com.example.demo.modules.pacientes.consumo;

import com.example.demo.modules.clinica.alta.EstadoAlta;
import com.example.demo.modules.clinica.alta.altaEntity;
import com.example.demo.modules.clinica.alta.altaRepository;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaDetalleEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaRepository;
import com.example.demo.modules.farmacia.salidas.TipoMovimientoSalida;
import com.example.demo.modules.pacientes.camas.camasEntity;
import com.example.demo.modules.pacientes.camas.camasRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumoEpisodioService {

    private static final int MINUTOS_POR_DIA = 24 * 60;

    private final EpisodioRepository episodioRepository;
    private final camasRepository camasRepository;
    private final altaRepository altaRepository;
    private final SolicitudSalidaRepository solicitudSalidaRepository;
    private final SolicitudSalidaMovimientoRepository movimientoRepository;

    /**
     * Consumo del episodio: productos entregados en Farmacia, descontando devoluciones, y la
     * estancia en cama calculada por periodos completos de 24 horas.
     */
    @Transactional(readOnly = true)
    public ConsumoEpisodioDTOs.Response obtenerConsumo(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        EpisodioEntity episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episodio no encontrado con el ID: " + episodioId));

        List<ConsumoEpisodioDTOs.ItemResponse> items = new ArrayList<>(consumoProductos(episodioId));
        ConsumoEpisodioDTOs.ItemResponse cama = consumoCama(episodio);
        if (cama != null) {
            items.add(cama);
        }

        BigDecimal total = items.stream()
                .map(ConsumoEpisodioDTOs.ItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new ConsumoEpisodioDTOs.Response(episodioId, List.copyOf(items), total);
    }

    private List<ConsumoEpisodioDTOs.ItemResponse> consumoProductos(Long episodioId) {
        Map<String, Acumulado> acumulados = new LinkedHashMap<>();

        for (SolicitudSalidaEntity solicitud :
                solicitudSalidaRepository.findByEpisodio_IdOrderByFechaSolicitudAsc(episodioId)) {
            Map<Long, List<SolicitudSalidaMovimientoEntity>> movimientosPorDetalle =
                    agruparMovimientos(solicitud);

            for (SolicitudSalidaDetalleEntity detalle : solicitud.getDetalles()) {
                Acumulado acumulado = acumulados.computeIfAbsent(
                        clave(detalle),
                        clave -> new Acumulado(detalle.getTipo(), detalle.getNombre()));

                for (SolicitudSalidaMovimientoEntity movimiento :
                        movimientosPorDetalle.getOrDefault(detalle.getId(), List.of())) {
                    int cantidad = valor(movimiento.getCantidad());
                    if (cantidad <= 0) {
                        continue;
                    }

                    BigDecimal precio = precio(movimiento.getLote());
                    BigDecimal monto = precio.multiply(BigDecimal.valueOf(cantidad));

                    if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.ENTREGA) {
                        acumulado.cantidad += cantidad;
                        acumulado.monto = acumulado.monto.add(monto);
                    } else if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.DEVOLUCION) {
                        acumulado.cantidad -= cantidad;
                        acumulado.monto = acumulado.monto.subtract(monto);
                    }
                }
            }
        }

        return acumulados.values().stream()
                .filter(acumulado -> acumulado.cantidad > 0)
                .sorted(Comparator
                        .comparing(Acumulado::tipo)
                        .thenComparing(Acumulado::nombre, String.CASE_INSENSITIVE_ORDER))
                .map(acumulado -> {
                    BigDecimal subtotal = acumulado.monto.max(BigDecimal.ZERO)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal precioUnitario = subtotal.divide(
                            BigDecimal.valueOf(acumulado.cantidad), 2, RoundingMode.HALF_UP);

                    return new ConsumoEpisodioDTOs.ItemResponse(
                            etiquetaTipo(acumulado.tipo),
                            acumulado.nombre,
                            null,
                            acumulado.cantidad,
                            acumulado.cantidad == 1 ? "unidad" : "unidades",
                            precioUnitario,
                            subtotal);
                })
                .toList();
    }

    private Map<Long, List<SolicitudSalidaMovimientoEntity>> agruparMovimientos(
            SolicitudSalidaEntity solicitud) {
        Map<Long, List<SolicitudSalidaMovimientoEntity>> movimientosPorDetalle = new LinkedHashMap<>();

        for (SolicitudSalidaMovimientoEntity movimiento :
                movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(solicitud.getId())) {
            if (movimiento.getDetalle() == null || movimiento.getDetalle().getId() == null) {
                continue;
            }

            movimientosPorDetalle
                    .computeIfAbsent(movimiento.getDetalle().getId(), clave -> new ArrayList<>())
                    .add(movimiento);
        }

        return movimientosPorDetalle;
    }

    private ConsumoEpisodioDTOs.ItemResponse consumoCama(EpisodioEntity episodio) {
        if (episodio.getPaciente() == null) {
            return null;
        }

        camasEntity cama = camasRepository.findByPaciente_Id(episodio.getPaciente().getId())
                .orElse(null);
        if (cama == null) {
            return null;
        }

        LocalDateTime fin = altaRepository
                .findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                        episodio.getId(), EstadoAlta.VIGENTE)
                .map(altaEntity::getFechaHoraEgreso)
                .orElseGet(LocalDateTime::now);

        int dias = diasUso(episodio.getFechaHoraIngreso(), fin);
        BigDecimal precio = cama.getPrecio() == null ? BigDecimal.ZERO : cama.getPrecio();
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(dias))
                .setScale(2, RoundingMode.HALF_UP);

        return new ConsumoEpisodioDTOs.ItemResponse(
                "Cama",
                "Cama " + cama.getCodigo(),
                detalleCama(cama),
                dias,
                dias == 1 ? "día" : "días",
                precio.setScale(2, RoundingMode.HALF_UP),
                subtotal);
    }

    private int diasUso(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            return 1;
        }

        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos <= 0) {
            return 1;
        }

        return Math.max(1, (int) Math.ceil(minutos / (double) MINUTOS_POR_DIA));
    }

    private String detalleCama(camasEntity cama) {
        List<String> partes = new ArrayList<>();
        if (cama.getHabitacion() != null && texto(cama.getHabitacion().getNombre()) != null) {
            partes.add("Habitación " + texto(cama.getHabitacion().getNombre()));
        }
        if (cama.getArea() != null && texto(cama.getArea().getNombre()) != null) {
            partes.add(texto(cama.getArea().getNombre()));
        }
        if (cama.getTipo() != null && texto(cama.getTipo().getNombre()) != null) {
            partes.add(texto(cama.getTipo().getNombre()));
        }
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }

    private String etiquetaTipo(TipoRecursoProcedimiento tipo) {
        if (tipo == TipoRecursoProcedimiento.MEDICAMENTO) {
            return "Medicamento";
        }
        if (tipo == TipoRecursoProcedimiento.INSUMO) {
            return "Insumo";
        }
        return "Producto";
    }

    private String clave(SolicitudSalidaDetalleEntity detalle) {
        return detalle.getTipo() + "#" + detalle.getReferenciaId() + "#" + detalle.getNombre();
    }

    private BigDecimal precio(LoteEntity lote) {
        return lote != null && lote.getPrecioVenta() != null
                ? lote.getPrecioVenta()
                : BigDecimal.ZERO;
    }

    private int valor(Integer cantidad) {
        return cantidad == null ? 0 : cantidad;
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static final class Acumulado {
        private final TipoRecursoProcedimiento tipo;
        private final String nombre;
        private int cantidad;
        private BigDecimal monto = BigDecimal.ZERO;

        private Acumulado(TipoRecursoProcedimiento tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre == null || nombre.isBlank() ? "Producto" : nombre;
        }

        private TipoRecursoProcedimiento tipo() {
            return tipo;
        }

        private String nombre() {
            return nombre;
        }
    }
}
