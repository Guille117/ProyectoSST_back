package com.example.demo.modules.farmacia.bajas;

import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bajas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BajaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteEntity lote;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "motivo_baja_id", nullable = false)
    private motivoBajaEntity motivoBaja;

    @Column(length = 500)
    private String observacion;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;
}
