package com.example.demo.modules.usuarios.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
