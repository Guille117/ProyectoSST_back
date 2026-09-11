package com.example.demo.modules.reportes.reportes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<ReporteEntity, Long> {

    Optional<ReporteEntity> findByNombreIgnoreCase(String nombre);

    List<ReporteEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ReporteEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<ReporteEntity> findByEstado(boolean estado);

    Optional<ReporteEntity> findByCodigo(String codigo);
}
