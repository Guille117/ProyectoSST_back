package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface aplicacionMedicamentoRepository extends JpaRepository<aplicacionMedicamentoEntity, Long> {

    List<aplicacionMedicamentoEntity> findByIndicacion_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long indicacionId);

    List<aplicacionMedicamentoEntity> findByIndicacion_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long episodioId);

    boolean existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalse(
            Long indicacionId, LocalDateTime fechaHoraProgramada);

    boolean existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalseAndIdNot(
            Long indicacionId, LocalDateTime fechaHoraProgramada, Long id);

    /** Cantidad total administrada de un medicamento del catálogo dentro del episodio. */
    @Query("""
            SELECT COALESCE(SUM(a.cantidad), 0)
            FROM aplicacionMedicamentoEntity a
            WHERE a.indicacion.medicamentoLog.id = :medicamentoLogId
              AND a.indicacion.episodio.id = :episodioId
              AND a.estado = :estado
              AND a.eliminado = false
            """)
    Long sumarCantidadAdministrada(
            @Param("episodioId") Long episodioId,
            @Param("medicamentoLogId") Long medicamentoLogId,
            @Param("estado") EstadoAplicacionMedicamento estado);
}
