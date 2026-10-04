package com.example.demo.modules.farmacia.medicamentoLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface medicamentoLogRepository extends JpaRepository<medicamentoLogEntity, Long> {
    @Query("""
        SELECT m FROM medicamentoLogEntity m
        WHERE LOWER(m.nombre) = LOWER(:nombre)
          AND m.dosis = :dosis
          AND m.unidadMedida.id = :unidadMedidaId
          AND m.marca.id = :marcaId
          AND m.viaAdmin.id = :viaAdminId
          AND m.presentacion.id = :presentacionId
        """)
    Optional<medicamentoLogEntity> findByIdentidad(
        @Param("nombre") String nombre,
        @Param("dosis") BigDecimal dosis,
        @Param("unidadMedidaId") Long unidadMedidaId,
        @Param("marcaId") Long marcaId,
        @Param("viaAdminId") Long viaAdminId,
        @Param("presentacionId") Long presentacionId
    );
    @Query("""
        SELECT m FROM medicamentoLogEntity m
        WHERE m.estado = :activo
          AND (:nombre IS NULL OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:marcaId IS NULL OR m.marca.id = :marcaId)
          AND (:presentacionId IS NULL OR m.presentacion.id = :presentacionId)
          AND (:viaAdminId IS NULL OR m.viaAdmin.id = :viaAdminId)
        """)
    List<medicamentoLogEntity> buscar(
        @Param("nombre") String nombre,
        @Param("marcaId") Long marcaId,
        @Param("presentacionId") Long presentacionId,
        @Param("viaAdminId") Long viaAdminId,
        @Param("activo") boolean activo
    );
    List<medicamentoLogEntity> findByEstado(boolean estado);
    Optional<medicamentoLogEntity> findByIdAndEstado(Long id, boolean estado);
    boolean existsByMarca_Id(Long marcaId);
    boolean existsByPresentacion_Id(Long presentacionId);
    boolean existsByUnidadMedida_Id(Long unidadMedidaId);
    boolean existsByViaAdmin_Id(Long viaAdminId);
}