package com.example.demo.modules.farmacia.lotes;

import com.example.demo.modules.farmacia.compras.CompraEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lotes", uniqueConstraints = @UniqueConstraint(
        name = "uk_lote_codigo_compra", columnNames = {"codigo_lote", "compra_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_lote", nullable = false, length = 30)
    private String codigoLote;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    @Builder.Default
    private Integer disponible = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    /** Opcional en lotes de insumo; obligatoria en lotes de medicamento (validado en LoteService). */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "precio_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_log_id")
    private insumosLogEntity insumoLog;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_log_id")
    private medicamentoLogEntity medicamentoLog;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private CompraEntity compra;
}
