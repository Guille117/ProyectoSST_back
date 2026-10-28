package com.example.demo.modules.farmacia.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<CompraEntity, Long> {

    List<CompraEntity> findByEstado(boolean estado);

    Optional<CompraEntity> findByCodigo(String codigo);

    List<CompraEntity> findByCodigoContainingIgnoreCase(String codigo);

    List<CompraEntity> findByCodigoContainingIgnoreCaseAndEstado(String codigo, boolean estado);
}
