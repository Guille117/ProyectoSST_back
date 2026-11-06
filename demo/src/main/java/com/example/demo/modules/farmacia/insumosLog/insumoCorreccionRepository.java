package com.example.demo.modules.farmacia.insumosLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface insumoCorreccionRepository extends JpaRepository<insumoCorreccionEntity, Long> {
}
