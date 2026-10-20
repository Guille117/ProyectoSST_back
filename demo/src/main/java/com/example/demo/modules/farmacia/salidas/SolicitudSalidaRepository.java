package com.example.demo.modules.farmacia.salidas;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudSalidaRepository extends JpaRepository<SolicitudSalidaEntity, Long> {

    Optional<SolicitudSalidaEntity> findByProcedimientoMedico_Id(Long procedimientoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SolicitudSalidaEntity s WHERE s.id = :id")
    Optional<SolicitudSalidaEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SolicitudSalidaEntity s WHERE s.procedimientoMedico.id = :procedimientoId")
    Optional<SolicitudSalidaEntity> findByProcedimientoMedico_IdForUpdate(
            @Param("procedimientoId") Long procedimientoId);

    @EntityGraph(attributePaths = {
            "procedimientoMedico",
            "procedimientoMedico.procedimiento",
            "episodio",
            "episodio.paciente",
            "episodio.paciente.persona",
            "detalles"
    })
    @Query("SELECT s FROM SolicitudSalidaEntity s WHERE s.id = :id")
    Optional<SolicitudSalidaEntity> findByIdConDetalles(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "procedimientoMedico",
            "procedimientoMedico.procedimiento",
            "episodio",
            "episodio.paciente",
            "episodio.paciente.persona",
            "detalles"
    })
    List<SolicitudSalidaEntity> findAllByOrderByFechaSolicitudDesc();

    @EntityGraph(attributePaths = {
            "procedimientoMedico",
            "procedimientoMedico.procedimiento",
            "episodio",
            "episodio.paciente",
            "episodio.paciente.persona",
            "detalles"
    })
    List<SolicitudSalidaEntity> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitudSalida estado);
}
