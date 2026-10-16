package com.example.demo.modules.clinica.examenFisico;

import com.example.demo.modules.clinica.parteCuerpo.parteCuerpoEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "examenes_fisicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class examenFisicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    // La fecha y hora las registra el servidor al crear el examen físico.
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean estado = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "parte_cuerpo_id", nullable = false)
    private parteCuerpoEntity parteCuerpo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;
}
