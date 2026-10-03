package com.example.demo.modules.pacientes.paciente;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodioRepository extends JpaRepository<EpisodioEntity, Long> {
	List<EpisodioEntity> findByPaciente_IdInOrderByIdDesc(List<Long> pacienteIds);
}