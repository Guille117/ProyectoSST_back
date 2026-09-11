package com.example.demo.modules.enfermeria.nose3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnfermeriaNose3Repository extends JpaRepository<EnfermeriaNose3Entity, Long> {

    Optional<EnfermeriaNose3Entity> findByNombreIgnoreCase(String nombre);

    List<EnfermeriaNose3Entity> findByNombreContainingIgnoreCase(String nombre);

    List<EnfermeriaNose3Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<EnfermeriaNose3Entity> findByEstado(boolean estado);

    Optional<EnfermeriaNose3Entity> findByCodigo(String codigo);
}
