package com.example.demo.modules.enfermeria.nose1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnfermeriaNose1Repository extends JpaRepository<EnfermeriaNose1Entity, Long> {

    Optional<EnfermeriaNose1Entity> findByNombreIgnoreCase(String nombre);

    List<EnfermeriaNose1Entity> findByNombreContainingIgnoreCase(String nombre);

    List<EnfermeriaNose1Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<EnfermeriaNose1Entity> findByEstado(boolean estado);

    Optional<EnfermeriaNose1Entity> findByCodigo(String codigo);
}
