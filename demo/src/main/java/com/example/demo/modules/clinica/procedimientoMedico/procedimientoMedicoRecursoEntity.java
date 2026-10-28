package com.example.demo.modules.clinica.procedimientoMedico;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "procedimientos_medicos_recursos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class procedimientoMedicoRecursoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procedimiento_medico_id", nullable = false)
    private procedimientoMedicoEntity procedimientoMedico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecursoProcedimiento tipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(nullable = false, length = 500)
    private String nombre;

    @Column(nullable = false)
    private Integer cantidad;
}
