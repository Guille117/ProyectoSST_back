package com.example.demo.modules.farmacia.salidas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudSalidaMovimientoRepository
        extends JpaRepository<SolicitudSalidaMovimientoEntity, Long> {

    List<SolicitudSalidaMovimientoEntity> findByDetalle_Solicitud_IdOrderByFechaHoraAsc(Long solicitudId);

    List<SolicitudSalidaMovimientoEntity> findByDetalle_IdOrderByFechaHoraAsc(Long detalleId);

    boolean existsByDetalle_Id(Long detalleId);
}
