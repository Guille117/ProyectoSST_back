package com.example.demo.modules.pacientes.paciente;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expedientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private PacienteEntity paciente;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}