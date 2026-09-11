package com.example.demo.modules.usuarios.horarios;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "horarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "es_rotativo", nullable = false)
    @Builder.Default
    private boolean esRotativo = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HorarioSemanalDetalleEntity> semanalDetalles = new ArrayList<>();

    @OneToOne(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    private HorarioTurnoDetalleEntity turnoDetalle;
}
