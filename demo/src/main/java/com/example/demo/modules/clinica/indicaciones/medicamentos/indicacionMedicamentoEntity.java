package com.example.demo.modules.clinica.indicaciones.medicamentos;

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
public class indicacionMedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "medicamento_log_id", nullable = true)
    private medicamentoLogEntity medicamentoLog;

    /** Nombre y detalles escritos por el usuario cuando el medicamento no está en el catálogo. */
    @Column(name = "medicamento_libre", length = 1000)
    private String medicamentoLibre;

    @Column(length = 255)
    private String presentacion;

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

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoIndicacionMedicamento estado = EstadoIndicacionMedicamento.ACTIVO;
}
