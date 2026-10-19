package com.example.demo.modules.clinica.procedimientoMedico;

import com.example.demo.modules.clinica.procedimiento.procedimientoEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "procedimientos_medicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class procedimientoMedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "procedimiento_id", nullable = false)
    private procedimientoEntity procedimiento;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(length = 2000)
    private String resultado;

    @Column(nullable = false)
    private LocalDateTime inicio;

    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProcedimientoMedico estado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @OneToMany(mappedBy = "procedimientoMedico", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<procedimientoMedicoRecursoEntity> recursos = new ArrayList<>();

    public void agregarRecurso(procedimientoMedicoRecursoEntity recurso) {
        recurso.setProcedimientoMedico(this);
        this.recursos.add(recurso);
    }
}
