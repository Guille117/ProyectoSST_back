package com.example.demo.modules.clinica.evolucion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface evolucionRepository extends JpaRepository<evolucionEntity, Long> {

    List<evolucionEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(
            Long episodioId, EstadoEvolucion estado);
}
