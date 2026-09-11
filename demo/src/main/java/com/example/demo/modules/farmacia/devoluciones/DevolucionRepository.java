package com.example.demo.modules.farmacia.devoluciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevolucionRepository extends JpaRepository<DevolucionEntity, Long> {

    Optional<DevolucionEntity> findByNombreIgnoreCase(String nombre);

    List<DevolucionEntity> findByNombreContainingIgnoreCase(String nombre);

    List<DevolucionEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<DevolucionEntity> findByEstado(boolean estado);

    Optional<DevolucionEntity> findByCodigo(String codigo);
}
