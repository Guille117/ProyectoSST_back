package com.example.demo.modules.farmacia.medicamentoLog;

import java.math.BigDecimal;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicamentos_log", uniqueConstraints = @UniqueConstraint(
    name = "uk_medicamento_log_identidad",
    columnNames = {"nombre", "dosis", "unidad_medida_id", "marca_id", "via_admin_id", "presentacion_id"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class medicamentoLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal dosis;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean estado = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private unidadMedidaEntity unidadMedida;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private marcaEntity marca;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "via_admin_id", nullable = false)
    private viaAdminEntity viaAdmin;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "presentacion_id", nullable = false)
    private presentacionEntity presentacion;
}