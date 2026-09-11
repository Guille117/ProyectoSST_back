package com.example.demo.modules.usuarios.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuestoRepository extends JpaRepository<PuestoEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    Optional<PuestoEntity> findByNombreIgnoreCase(String nombre);

    List<PuestoEntity> findByNombreContainingIgnoreCase(String nombre);
    List<PuestoEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<PuestoEntity> findByEstado(boolean estado);

    boolean existsByCodigo(String codigo);
    Optional<PuestoEntity> findByCodigo(String codigo);
}
