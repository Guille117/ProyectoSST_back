package com.example.demo.modules.pacientes.camas;

import com.example.demo.modules.pacientes.area.areaEntity;
import com.example.demo.modules.pacientes.habitaciones.habitacionesEntity;
import com.example.demo.modules.pacientes.tipoCama.tipoCamaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "camas", uniqueConstraints = @UniqueConstraint(name = "uk_cama_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class camasEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private habitacionesEntity habitacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_cama_id", nullable = false)
    private tipoCamaEntity tipo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private areaEntity area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCama estado = EstadoCama.DISPONIBLE;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}