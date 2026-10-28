package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solicitudes_salida_detalle", uniqueConstraints = @UniqueConstraint(
        name = "uk_solicitud_salida_detalle_item",
        columnNames = {"solicitud_id", "tipo", "referencia_id"}), indexes = @Index(
        name = "idx_solicitud_salida_detalle_solicitud", columnList = "solicitud_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSalidaDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudSalidaEntity solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecursoProcedimiento tipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(nullable = false, length = 500)
    private String nombre;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "cantidad_entregada", nullable = false)
    @Builder.Default
    private Integer cantidadEntregada = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDetalleSalida estado;

    public int pendiente() {
        int solicitada = cantidadSolicitada == null ? 0 : cantidadSolicitada;
        int entregada = cantidadEntregada == null ? 0 : cantidadEntregada;
        return Math.max(0, solicitada - entregada);
    }
}
