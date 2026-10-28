package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "signos_vitales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class signosVitalesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer sistolica;

    @Column(nullable = false)
    private Integer diastolica;

    @Column(name = "frecuencia_cardiaca", nullable = false)
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria", nullable = false)
    private Integer frecuenciaRespiratoria;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(nullable = false)
    private Integer saturacion;

    @Column(length = 2000)
    private String observacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
