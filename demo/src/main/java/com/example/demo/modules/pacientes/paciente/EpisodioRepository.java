package com.example.demo.modules.pacientes.paciente;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodioRepository extends JpaRepository<EpisodioEntity, Long> {
	List<EpisodioEntity> findByPaciente_IdInOrderByIdDesc(List<Long> pacienteIds);

	Optional<EpisodioEntity> findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(Long pacienteId);

	Optional<EpisodioEntity> findFirstByPaciente_IdOrderByIdDesc(Long pacienteId);

	boolean existsByPaciente_IdAndEstadoTrue(Long pacienteId);
}