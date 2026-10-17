package com.example.demo.modules.clinica.indicaciones.indicacionMedica;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "indicaciones_medicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class indicacionMedicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medicamento_log_id", nullable = false)
    private medicamentoLogEntity medicamentoLog;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer frecuencia;

    private Integer duracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_duracion", nullable = false, length = 20)
    private TipoDuracion tipoDuracion;

    @Column(length = 2000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoIndicacionMedica estado = EstadoIndicacionMedica.ACTIVO;
}
