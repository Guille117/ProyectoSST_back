package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_episodio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoEpisodioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoDocumentoEpisodio tipo;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;
}
