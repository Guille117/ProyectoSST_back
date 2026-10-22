package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface signosVitalesRepository extends JpaRepository<signosVitalesEntity, Long> {

    List<signosVitalesEntity> findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(Long episodioId);
}
