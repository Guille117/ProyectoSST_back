package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_hidrico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class controlHidricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRegistroHidrico tipo;

    @Column(nullable = false, length = 100)
    private String via;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 500)
    private String descripcion;

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
