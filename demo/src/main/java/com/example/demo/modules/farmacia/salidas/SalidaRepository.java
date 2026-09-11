package com.example.demo.modules.farmacia.salidas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalidaRepository extends JpaRepository<SalidaEntity, Long> {

    Optional<SalidaEntity> findByNombreIgnoreCase(String nombre);

    List<SalidaEntity> findByNombreContainingIgnoreCase(String nombre);

    List<SalidaEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<SalidaEntity> findByEstado(boolean estado);

    Optional<SalidaEntity> findByCodigo(String codigo);
}
