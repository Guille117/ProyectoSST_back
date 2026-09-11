package com.example.demo.modules.enfermeria.nose2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnfermeriaNose2Repository extends JpaRepository<EnfermeriaNose2Entity, Long> {

    Optional<EnfermeriaNose2Entity> findByNombreIgnoreCase(String nombre);

    List<EnfermeriaNose2Entity> findByNombreContainingIgnoreCase(String nombre);

    List<EnfermeriaNose2Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<EnfermeriaNose2Entity> findByEstado(boolean estado);

    Optional<EnfermeriaNose2Entity> findByCodigo(String codigo);
}
