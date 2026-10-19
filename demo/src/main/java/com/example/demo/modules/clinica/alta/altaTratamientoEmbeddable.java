package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import jakarta.persistence.*;
import lombok.*;

/** Tratamiento indicado en el alta: un medicamento o un cuidado con su duración. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class altaTratamientoEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTratamiento tipo;

    @Column(nullable = false, length = 500)
    private String nombre;

    @Column(name = "medicamento_log_id")
    private Long medicamentoLogId;

    @Column(name = "medicamento_libre", length = 1000)
    private String medicamentoLibre;

    private Integer cantidad;

    @Column(name = "unidad_medida", length = 100)
    private String unidadMedida;

    @Column(nullable = false)
    private Integer frecuencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "duracion_tipo", nullable = false, length = 20)
    private TipoDuracion duracionTipo;

    private Integer duracion;

    @Column(length = 2000)
    private String observaciones;
}
