package com.example.demo.modules.farmacia.insumosLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface insumosLogRepository extends JpaRepository<insumosLogEntity, Long> {
    Optional<insumosLogEntity> findByNombreIgnoreCase(String nombre);
    List<insumosLogEntity> findByNombreContainingIgnoreCase(String nombre);
    List<insumosLogEntity> findByEstado(boolean estado);
    List<insumosLogEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);
    List<insumosLogEntity> findByMarca_IdAndEstado(Long marcaId, boolean estado);
    List<insumosLogEntity> findByNombreContainingIgnoreCaseAndMarca_IdAndEstado(
            String nombre, Long marcaId, boolean estado);
    boolean existsByMarca_Id(Long marcaId);
}