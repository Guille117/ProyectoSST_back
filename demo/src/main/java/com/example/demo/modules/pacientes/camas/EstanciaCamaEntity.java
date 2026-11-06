package com.example.demo.modules.pacientes.camas;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estancias_cama", indexes = {
        @Index(name = "idx_estancia_cama_episodio", columnList = "episodio_id"),
        @Index(name = "idx_estancia_cama_fin", columnList = "fecha_hora_fin")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstanciaCamaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cama_id", nullable = false)
    private camasEntity cama;

    /** Precio diario de la cama congelado al iniciar el tramo. */
    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "fecha_hora_inicio", nullable = false, updatable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;
}
