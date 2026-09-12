package com.example.demo.modules.farmacia.insumosLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface insumosLogRepository extends JpaRepository<insumosLogEntity, Long> {
    Optional<insumosLogEntity> findByNombreIgnoreCase(String nombre);
    List<insumosLogEntity> findByNombreContainingIgnoreCase(String nombre);
}