package com.example.demo.modules.bitacora;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBitacora tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionBitacora accion;

    @Column(length = 50)
    private String modulo;

    @Column(length = 80)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "paciente_id")
    private Long pacienteId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nombre", length = 50)
    private String usuarioNombre;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Lob
    @Column(name = "detalle")
    private String detalle;

    @Column(length = 45)
    private String ip;
}
