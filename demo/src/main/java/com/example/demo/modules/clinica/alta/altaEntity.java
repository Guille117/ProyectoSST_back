package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoEntity;
import com.example.demo.modules.pacientes.institucion.institucionEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "altas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class altaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alta", nullable = false, length = 30)
    private TipoAlta tipoAlta;

    @Column(name = "diagnostico_egreso", nullable = false, length = 1000)
    private String diagnosticoEgreso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "condicion_egreso_id")
    private condicionEgresoEntity condicionEgreso;

    @Column(name = "observacion_egreso", length = 1000)
    private String observacionEgreso;

    @Column(name = "fecha_hora_egreso", nullable = false, updatable = false)
    private LocalDateTime fechaHoraEgreso;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoAlta estado = EstadoAlta.VIGENTE;

    // ------------------- RETIRO VOLUNTARIO -------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "responsable_retiro", length = 30)
    private ResponsableRetiro responsableRetiro;

    @Column(name = "motivo_retiro", length = 1000)
    private String motivoRetiro;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alta_riesgos", joinColumns = @JoinColumn(name = "alta_id"))
    @Column(name = "riesgo", nullable = false, length = 500)
    @OrderColumn(name = "orden")
    @Builder.Default
    private List<String> riesgosRetiro = new ArrayList<>();

    // ------------------- REFERENCIA / TRASLADO -------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institucion_id")
    private institucionEntity institucion;

    @Column(name = "motivo_referencia", length = 1000)
    private String motivoReferencia;

    @Column(name = "indicaciones_traslado", length = 1000)
    private String indicacionesTraslado;

    // ------------------- TRATAMIENTO -------------------
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alta_tratamientos", joinColumns = @JoinColumn(name = "alta_id"))
    @OrderColumn(name = "orden")
    @Builder.Default
    private List<altaTratamientoEmbeddable> tratamientos = new ArrayList<>();
}
