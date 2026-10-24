package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_salida", uniqueConstraints = @UniqueConstraint(
        name = "uk_solicitud_salida_procedimiento",
        columnNames = "procedimiento_medico_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSalidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    /** Opcional: una salida directa de Farmacia o una solicitud de Enfermería no tiene procedimiento. */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "procedimiento_medico_id", nullable = true, unique = true)
    private procedimientoMedicoEntity procedimientoMedico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "episodio_id", nullable = false)
    private EpisodioEntity episodio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_solicita_id", nullable = false)
    private UsuarioEntity usuarioSolicita;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudSalida estado;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SolicitudSalidaDetalleEntity> detalles = new ArrayList<>();

    public void agregarDetalle(SolicitudSalidaDetalleEntity detalle) {
        detalle.setSolicitud(this);
        this.detalles.add(detalle);
    }
}
