package com.example.demo.modules.pacientes.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<ExpedienteEntity, Long> {
    Optional<ExpedienteEntity> findByPacienteId(Long pacienteId);

    List<ExpedienteEntity> findByPaciente_IdIn(List<Long> pacienteIds);

    List<ExpedienteEntity> findByCodigoIsNullOrderByIdAsc();
}