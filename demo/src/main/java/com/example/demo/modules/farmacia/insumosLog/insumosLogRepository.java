package com.example.demo.modules.farmacia.insumosLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
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

    boolean existsByCodigo(String codigo);

    /** Bloquea la fila del insumo para serializar la deshabilitación con el alta de existencias. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM insumosLogEntity i WHERE i.id = :id")
    Optional<insumosLogEntity> findByIdForUpdate(@Param("id") Long id);
}