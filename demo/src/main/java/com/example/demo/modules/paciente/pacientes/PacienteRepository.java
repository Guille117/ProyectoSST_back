package com.example.demo.modules.paciente.pacientes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    Optional<PacienteEntity> findByNombreIgnoreCase(String nombre);

    List<PacienteEntity> findByNombreContainingIgnoreCase(String nombre);

    List<PacienteEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<PacienteEntity> findByEstado(boolean estado);

    Optional<PacienteEntity> findByCodigo(String codigo);
}
