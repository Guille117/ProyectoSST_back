package com.example.demo.modules.medicina.nose1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicinaNose1Repository extends JpaRepository<MedicinaNose1Entity, Long> {

    Optional<MedicinaNose1Entity> findByNombreIgnoreCase(String nombre);

    List<MedicinaNose1Entity> findByNombreContainingIgnoreCase(String nombre);

    List<MedicinaNose1Entity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<MedicinaNose1Entity> findByEstado(boolean estado);

    Optional<MedicinaNose1Entity> findByCodigo(String codigo);
}
