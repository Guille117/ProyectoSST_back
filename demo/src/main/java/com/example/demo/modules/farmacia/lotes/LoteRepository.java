package com.example.demo.modules.farmacia.lotes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT COALESCE(SUM(l.total), 0) FROM LoteEntity l WHERE l.compra.id = :compraId")
    BigDecimal sumTotalesPorCompra(@Param("compraId") Long compraId);
}
