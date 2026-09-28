package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personas_responsables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponsableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private PersonaEntity persona;

    @Column(nullable = false, length = 50)
    private String parentesco;

    @Column(length = 255)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}