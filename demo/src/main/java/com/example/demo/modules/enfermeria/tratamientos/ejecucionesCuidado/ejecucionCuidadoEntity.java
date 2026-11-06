package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoEntity;
import com.example.demo.modules.enfermeria.monitoreo.controlHidrico.controlHidricoEntity;
import com.example.demo.modules.enfermeria.monitoreo.glucosa.glucosaEntity;
import com.example.demo.modules.enfermeria.monitoreo.signosVitales.signosVitalesEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ejecuciones_cuidado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ejecucionCuidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cuidado_id", nullable = false)
    private cuidadoEntity cuidado;

    @Column(name = "fecha_hora_programada", nullable = false)
    private LocalDateTime fechaHoraProgramada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEjecucionCuidado estado;

    @Column(length = 2000)
    private String observacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signos_vitales_id")
    private signosVitalesEntity signosVitales;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "glucosa_id")
    private glucosaEntity glucosa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "control_hidrico_id")
    private controlHidricoEntity controlHidrico;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
