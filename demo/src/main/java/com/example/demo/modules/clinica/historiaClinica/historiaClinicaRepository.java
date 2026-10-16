package com.example.demo.modules.clinica.historiaClinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface historiaClinicaRepository extends JpaRepository<historiaClinicaEntity, Long> {

    List<historiaClinicaEntity> findByEstadoOrderByFechaDesc(boolean estado);

    List<historiaClinicaEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, boolean estado);
}
