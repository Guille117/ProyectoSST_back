package com.example.demo.modules.enfermeria.monitoreo.glucosa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface glucosaRepository extends JpaRepository<glucosaEntity, Long> {

    List<glucosaEntity> findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(Long episodioId);
}
