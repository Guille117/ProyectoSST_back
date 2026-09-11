package com.example.demo.modules.farmacia.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<CompraEntity, Long> {

    Optional<CompraEntity> findByNombreIgnoreCase(String nombre);

    List<CompraEntity> findByNombreContainingIgnoreCase(String nombre);

    List<CompraEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<CompraEntity> findByEstado(boolean estado);

    Optional<CompraEntity> findByCodigo(String codigo);
}
