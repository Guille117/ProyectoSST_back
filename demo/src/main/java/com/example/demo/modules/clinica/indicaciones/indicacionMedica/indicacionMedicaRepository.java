package com.example.demo.modules.clinica.indicaciones.indicacionMedica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface indicacionMedicaRepository extends JpaRepository<indicacionMedicaEntity, Long> {

    List<indicacionMedicaEntity> findByEstadoOrderByFechaDesc(EstadoIndicacionMedica estado);

    List<indicacionMedicaEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, EstadoIndicacionMedica estado);
}
