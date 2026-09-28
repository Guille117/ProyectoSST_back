package com.example.demo.modules.pacientes.paciente;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "referencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_institucion", nullable = false, length = 150)
    private String nombreInstitucion;

    @Column(name = "motivo_referencia", nullable = false, length = 1000)
    private String motivoReferencia;

    @Column(name = "url_documento", length = 2048)
    private String urlDocumento;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}