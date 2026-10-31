package com.example.demo.modules.pacientes.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {
    Optional<PacienteEntity> findByPersonaId(Long personaId);

    List<PacienteEntity> findByEstadoOrderByIdAsc(boolean estado);

    List<PacienteEntity> findByIdInAndEstadoOrderByIdAsc(Collection<Long> ids, boolean estado);
}