package com.example.demo.modules.clinica.indicaciones.examenes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface examenRepository extends JpaRepository<examenEntity, Long> {

    List<examenEntity> findByEstadoOrderByFechaDesc(EstadoExamen estado);

    List<examenEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, EstadoExamen estado);
}
