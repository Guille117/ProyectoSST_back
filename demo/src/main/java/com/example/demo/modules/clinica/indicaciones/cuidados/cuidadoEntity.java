package com.example.demo.modules.clinica.indicaciones.cuidados;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuidados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class cuidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cuidado", nullable = false, length = 200)
    private String nombreCuidado;

    @Column(nullable = false)
    private Integer frecuencia;

    private Integer duracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_duracion", nullable = false, length = 20)
    private TipoDuracion tipoDuracion;

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

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCuidado estado = EstadoCuidado.EN_PROCESO;
}
