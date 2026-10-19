package com.example.demo.modules.clinica.alta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface altaRepository extends JpaRepository<altaEntity, Long> {

    Optional<altaEntity> findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
            Long episodioId, EstadoAlta estado);

    List<altaEntity> findByEstadoAndFechaHoraEgresoGreaterThanEqualOrderByFechaHoraEgresoDesc(
            EstadoAlta estado, LocalDateTime desde);
}
