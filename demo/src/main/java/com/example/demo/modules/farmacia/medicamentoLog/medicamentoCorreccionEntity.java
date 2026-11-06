package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Auditoría de correcciones justificadas del nombre de un medicamento del catálogo.
 * Conserva el valor anterior, el nuevo, el motivo, el usuario responsable y la fecha/hora.
 */
@Entity
@Table(name = "medicamentos_correcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class medicamentoCorreccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medicamento_log_id", nullable = false)
    private medicamentoLogEntity medicamentoLog;

    @Column(name = "nombre_anterior", nullable = false, length = 150)
    private String nombreAnterior;

    @Column(name = "nombre_nuevo", nullable = false, length = 150)
    private String nombreNuevo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
}
