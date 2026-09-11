package com.example.demo.modules.medicina.nose3;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicina_nose3")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicinaNose3Entity {

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
