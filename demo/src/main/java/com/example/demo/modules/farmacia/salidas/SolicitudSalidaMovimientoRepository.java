package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudSalidaMovimientoRepository
        extends JpaRepository<SolicitudSalidaMovimientoEntity, Long> {

    List<SolicitudSalidaMovimientoEntity> findByDetalle_Solicitud_IdOrderByFechaHoraAsc(Long solicitudId);

    List<SolicitudSalidaMovimientoEntity> findByDetalle_IdOrderByFechaHoraAsc(Long detalleId);

    boolean existsByDetalle_Id(Long detalleId);

    boolean existsByLote_Compra_Id(Long compraId);

    /** Suma de entregas o devoluciones de un recurso (medicamento/insumo) dentro del episodio. */
    @Query("""
            SELECT COALESCE(SUM(m.cantidad), 0)
            FROM SolicitudSalidaMovimientoEntity m
            WHERE m.detalle.solicitud.episodio.id = :episodioId
              AND m.detalle.tipo = :tipo
              AND m.detalle.referenciaId = :referenciaId
              AND m.tipoMovimiento = :movimiento
            """)
    Long sumarCantidadPorRecurso(
            @Param("episodioId") Long episodioId,
            @Param("tipo") TipoRecursoProcedimiento tipo,
            @Param("referenciaId") Long referenciaId,
            @Param("movimiento") TipoMovimientoSalida movimiento);
}
