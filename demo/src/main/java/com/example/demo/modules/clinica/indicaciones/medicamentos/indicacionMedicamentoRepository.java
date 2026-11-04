package com.example.demo.modules.clinica.indicaciones.medicamentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface indicacionMedicamentoRepository extends JpaRepository<indicacionMedicamentoEntity, Long> {

    List<indicacionMedicamentoEntity> findByEstadoOrderByFechaDesc(EstadoIndicacionMedicamento estado);

    List<indicacionMedicamentoEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, EstadoIndicacionMedicamento estado);

    /** Medicamentos del catálogo (sin duplicados) indicados en el episodio con el estado dado. */
    @Query("""
            SELECT DISTINCT i.medicamentoLog.id
            FROM indicacionMedicamentoEntity i
            WHERE i.episodio.id = :episodioId
              AND i.medicamentoLog IS NOT NULL
              AND i.estado = :estado
            """)
    List<Long> findDistinctMedicamentoLogIds(
            @Param("episodioId") Long episodioId,
            @Param("estado") EstadoIndicacionMedicamento estado);
}
