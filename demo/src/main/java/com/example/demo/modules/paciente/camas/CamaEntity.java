package com.example.demo.modules.paciente.camas;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "camas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
