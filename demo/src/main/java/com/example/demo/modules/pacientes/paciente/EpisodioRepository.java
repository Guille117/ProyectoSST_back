package com.example.demo.modules.pacientes.paciente;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodioRepository extends JpaRepository<EpisodioEntity, Long> {
	List<EpisodioEntity> findByPaciente_IdInOrderByIdDesc(List<Long> pacienteIds);

	@Query("select distinct e.paciente.id from EpisodioEntity e where e.medico.id = :medicoId and e.estado = true")
	List<Long> findPacienteIdsByMedicoIdAndEstadoTrue(@Param("medicoId") Long medicoId);

	boolean existsByPaciente_IdAndMedico_Id(Long pacienteId, Long medicoId);

	Optional<EpisodioEntity> findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(Long pacienteId);

	Optional<EpisodioEntity> findFirstByPaciente_IdOrderByIdDesc(Long pacienteId);

	List<EpisodioEntity> findByPaciente_IdOrderByIdDesc(Long pacienteId);

	boolean existsByPaciente_IdAndEstadoTrue(Long pacienteId);
}