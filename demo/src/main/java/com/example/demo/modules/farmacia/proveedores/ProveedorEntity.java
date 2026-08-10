package com.example.demo.modules.farmacia.proveedores;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, length = 15)
    private String nit;

    @Column(length = 8)
    private String telefono;

    @Column(unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
