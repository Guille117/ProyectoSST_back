package com.example.demo.modules.enfermeria.nose1;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enfermeria_nose1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnfermeriaNose1Entity {

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
