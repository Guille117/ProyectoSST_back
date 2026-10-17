package com.example.demo.modules.clinica.indicaciones.medicamentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface indicacionMedicamentoRepository extends JpaRepository<indicacionMedicamentoEntity, Long> {

    List<indicacionMedicamentoEntity> findByEstadoOrderByFechaDesc(EstadoIndicacionMedicamento estado);

    List<indicacionMedicamentoEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, EstadoIndicacionMedicamento estado);
}
