package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface aplicacionMedicamentoRepository extends JpaRepository<aplicacionMedicamentoEntity, Long> {

    List<aplicacionMedicamentoEntity> findByIndicacion_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long indicacionId);

    List<aplicacionMedicamentoEntity> findByIndicacion_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long episodioId);

    boolean existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalse(
            Long indicacionId, LocalDateTime fechaHoraProgramada);
}
