package com.example.demo.modules.clinica.examenFisico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface examenFisicoRepository extends JpaRepository<examenFisicoEntity, Long> {

    List<examenFisicoEntity> findByEstadoOrderByFechaDesc(boolean estado);

    List<examenFisicoEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, boolean estado);

    List<examenFisicoEntity> findByParteCuerpo_IdAndEstadoOrderByFechaDesc(Long parteCuerpoId, boolean estado);
}
