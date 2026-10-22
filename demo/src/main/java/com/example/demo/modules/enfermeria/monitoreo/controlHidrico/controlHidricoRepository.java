package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface controlHidricoRepository extends JpaRepository<controlHidricoEntity, Long> {

    List<controlHidricoEntity> findByEpisodio_IdOrderByFechaDesc(Long episodioId);
}
