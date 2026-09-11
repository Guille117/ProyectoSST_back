package com.example.demo.modules.medicina.nose3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicinaNose3Repository extends JpaRepository<MedicinaNose3Entity, Long> {

    Optional<MedicinaNose3Entity> findByNombreIgnoreCase(String nombre);

    List<MedicinaNose3Entity> findByNombreContainingIgnoreCase(String nombre);

    List<MedicinaNose3Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<MedicinaNose3Entity> findByEstado(boolean estado);

    Optional<MedicinaNose3Entity> findByCodigo(String codigo);
}
