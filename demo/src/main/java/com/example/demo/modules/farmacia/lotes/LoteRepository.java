package com.example.demo.modules.farmacia.lotes;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<LoteEntity, Long> {

    boolean existsByCodigoLoteAndCompra_Id(String codigoLote, Long compraId);

    Optional<LoteEntity> findByCodigoLoteIgnoreCaseAndCompra_Id(String codigoLote, Long compraId);

    List<LoteEntity> findByCodigoLoteIgnoreCase(String codigoLote);

    List<LoteEntity> findByEstado(boolean estado);

    List<LoteEntity> findByCodigoLoteContainingIgnoreCase(String codigoLote);

    List<LoteEntity> findByCodigoLoteContainingIgnoreCaseAndEstado(String codigoLote, boolean estado);

    List<LoteEntity> findByCompra_Id(Long compraId);

    List<LoteEntity> findByCompra_IdIn(List<Long> compraIds);

    List<LoteEntity> findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(
            Long medicamentoLogId, boolean estado, Integer disponible);

    List<LoteEntity> findByInsumoLog_IdAndEstadoAndDisponibleGreaterThan(
            Long insumoLogId, boolean estado, Integer disponible);

    @Query("SELECT COALESCE(SUM(l.total), 0) FROM LoteEntity l WHERE l.compra.id = :compraId")
    BigDecimal sumTotalesPorCompra(@Param("compraId") Long compraId);

    /** Unidades disponibles de un medicamento en lotes activos y no vencidos. */
    @Query("""
            SELECT COALESCE(SUM(l.disponible), 0)
            FROM LoteEntity l
            WHERE l.medicamentoLog.id = :medicamentoLogId
              AND l.estado = true
              AND (l.fechaVencimiento IS NULL OR l.fechaVencimiento >= :hoy)
            """)
    Long sumDisponibleMedicamento(
            @Param("medicamentoLogId") Long medicamentoLogId,
            @Param("hoy") LocalDate hoy);

    /** Unidades disponibles de un insumo en lotes activos y no vencidos. */
    @Query("""
            SELECT COALESCE(SUM(l.disponible), 0)
            FROM LoteEntity l
            WHERE l.insumoLog.id = :insumoLogId
              AND l.estado = true
              AND (l.fechaVencimiento IS NULL OR l.fechaVencimiento >= :hoy)
            """)
    Long sumDisponibleInsumo(
            @Param("insumoLogId") Long insumoLogId,
            @Param("hoy") LocalDate hoy);

    /** Bloquea el lote para descontar stock sin condiciones de carrera. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LoteEntity l WHERE l.id = :id")
    Optional<LoteEntity> findByIdForUpdate(@Param("id") Long id);
}
