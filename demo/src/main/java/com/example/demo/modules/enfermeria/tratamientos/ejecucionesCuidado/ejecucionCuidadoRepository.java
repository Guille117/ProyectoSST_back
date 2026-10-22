package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ejecucionCuidadoRepository extends JpaRepository<ejecucionCuidadoEntity, Long> {

    List<ejecucionCuidadoEntity> findByCuidado_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long cuidadoId);

    List<ejecucionCuidadoEntity> findByCuidado_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(
            Long episodioId);

    boolean existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(
            Long cuidadoId, LocalDateTime fechaHoraProgramada);
}
