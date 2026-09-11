package com.example.demo.modules.usuarios.horarios;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "horario_turno_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioTurnoDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false, unique = true)
    private HorarioEntity horario;

    @Column(name = "horas_trabajo", nullable = false)
    private Integer horasTrabajo;

    @Column(name = "horas_descanso", nullable = false)
    private Integer horasDescanso;
}
