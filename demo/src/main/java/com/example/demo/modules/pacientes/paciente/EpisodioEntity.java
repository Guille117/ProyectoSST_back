package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}