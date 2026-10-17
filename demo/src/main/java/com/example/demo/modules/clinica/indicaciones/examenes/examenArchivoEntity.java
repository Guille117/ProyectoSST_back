package com.example.demo.modules.clinica.indicaciones.examenes;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "examenes_archivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class examenArchivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "examen_id", nullable = false)
    private examenEntity examen;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;
}
