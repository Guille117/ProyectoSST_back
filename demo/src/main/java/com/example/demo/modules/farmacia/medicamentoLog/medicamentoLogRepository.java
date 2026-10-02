package com.example.demo.modules.farmacia.medicamentoLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface medicamentoLogRepository extends JpaRepository<medicamentoLogEntity, Long> {
    Optional<medicamentoLogEntity> findByNombreIgnoreCase(String nombre);
    List<medicamentoLogEntity> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByMarca_Id(Long marcaId);
    boolean existsByPresentacion_Id(Long presentacionId);
    boolean existsByUnidadMedida_Id(Long unidadMedidaId);
    boolean existsByViaAdmin_Id(Long viaAdminId);
}