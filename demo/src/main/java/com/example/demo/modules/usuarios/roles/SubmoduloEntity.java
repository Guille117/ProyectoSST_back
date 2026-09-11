package com.example.demo.modules.usuarios.roles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submodulos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmoduloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    private ModuloEntity modulo;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
