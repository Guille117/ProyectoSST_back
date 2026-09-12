package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.roles.RolEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private PersonaEntity persona;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "puesto_id", nullable = false)
    private puestoEntity puesto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioEntity horario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntity rol;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Null hasta que el usuario establece sus credenciales con el PIN de primer ingreso.
    @Column(length = 255)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
