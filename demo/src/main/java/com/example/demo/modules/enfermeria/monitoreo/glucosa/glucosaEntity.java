package com.example.demo.modules.enfermeria.monitoreo.glucosa;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "glucosa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class glucosaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer glucosa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMedicionGlucosa tipo;

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
