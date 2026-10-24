package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.pacientes.alergia.alergiaEntity;
import com.example.demo.modules.pacientes.enfermedad.enfermedadEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private PersonaEntity persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    private EstadoCivil estadoCivil;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ocupacion;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "paciente_alergias",
            joinColumns = @JoinColumn(name = "paciente_id"),
            inverseJoinColumns = @JoinColumn(name = "alergia_id"))
    @Builder.Default
    private Set<alergiaEntity> alergias = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "paciente_enfermedades",
            joinColumns = @JoinColumn(name = "paciente_id"),
            inverseJoinColumns = @JoinColumn(name = "enfermedad_id"))
    @Builder.Default
    private Set<enfermedadEntity> enfermedades = new LinkedHashSet<>();
}
