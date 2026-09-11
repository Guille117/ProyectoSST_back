package com.example.demo.modules.paciente.camas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CamaRepository extends JpaRepository<CamaEntity, Long> {

    Optional<CamaEntity> findByNombreIgnoreCase(String nombre);

    List<CamaEntity> findByNombreContainingIgnoreCase(String nombre);

    List<CamaEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<CamaEntity> findByEstado(boolean estado);

    Optional<CamaEntity> findByCodigo(String codigo);
}
