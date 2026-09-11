package com.example.demo.modules.usuarios.roles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_permisos", uniqueConstraints = {
        @UniqueConstraint(name = "uq_rol_submodulo", columnNames = {"rol_id", "submodulo_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermisoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntity rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submodulo_id", nullable = false)
    private SubmoduloEntity submodulo;

    @Column(name = "puede_leer", nullable = false)
    @Builder.Default
    private boolean puedeLeer = false;

    @Column(name = "puede_crear", nullable = false)
    @Builder.Default
    private boolean puedeCrear = false;

    @Column(name = "puede_editar", nullable = false)
    @Builder.Default
    private boolean puedeEditar = false;

    @Column(name = "puede_eliminar", nullable = false)
    @Builder.Default
    private boolean puedeEliminar = false;
}
