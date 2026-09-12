package com.example.demo.modules.farmacia.insumosLog;

import com.example.demo.modules.farmacia.marca.marcaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insumos_log", uniqueConstraints = @UniqueConstraint(name = "uk_insumo_log_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class insumosLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private marcaEntity marca;
}