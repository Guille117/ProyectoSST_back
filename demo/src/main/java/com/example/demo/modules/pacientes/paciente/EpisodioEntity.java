package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.pacientes.camas.camasEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episodios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atencion", nullable = false, length = 20)
    private TipoAtencion tipoAtencion;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    /** Fecha y hora en que el paciente ingresó al episodio de atención. */
    @Column(name = "fecha_hora_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaHoraIngreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PacienteEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediente_id", nullable = false)
    private ExpedienteEntity expediente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private UsuarioEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_responsable_id")
    private PersonaResponsableEntity personaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referencia_id")
    private ReferenciaEntity referencia;

    /** Cama utilizada durante el episodio; se conserva aunque el paciente ya haya egresado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cama_id")
    private camasEntity cama;

    /** Precio diario de la cama congelado al momento de asignarla al episodio. */
    @Column(name = "precio_cama", precision = 12, scale = 2)
    private BigDecimal precioCama;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    @OneToMany(mappedBy = "episodio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentoEpisodioEntity> documentos = new ArrayList<>();
}