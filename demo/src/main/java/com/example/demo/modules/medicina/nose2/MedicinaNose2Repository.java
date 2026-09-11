package com.example.demo.modules.medicina.nose2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicinaNose2Repository extends JpaRepository<MedicinaNose2Entity, Long> {

    Optional<MedicinaNose2Entity> findByNombreIgnoreCase(String nombre);

    List<MedicinaNose2Entity> findByNombreContainingIgnoreCase(String nombre);

    List<MedicinaNose2Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<MedicinaNose2Entity> findByEstado(boolean estado);

    Optional<MedicinaNose2Entity> findByCodigo(String codigo);
}
