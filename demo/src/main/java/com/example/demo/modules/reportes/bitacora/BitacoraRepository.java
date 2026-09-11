package com.example.demo.modules.reportes.bitacora;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BitacoraRepository extends JpaRepository<BitacoraEntity, Long> {

    Optional<BitacoraEntity> findByNombreIgnoreCase(String nombre);

    List<BitacoraEntity> findByNombreContainingIgnoreCase(String nombre);

    List<BitacoraEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<BitacoraEntity> findByEstado(boolean estado);

    Optional<BitacoraEntity> findByCodigo(String codigo);
}
