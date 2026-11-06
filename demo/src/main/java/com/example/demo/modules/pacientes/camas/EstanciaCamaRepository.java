package com.example.demo.modules.pacientes.camas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstanciaCamaRepository extends JpaRepository<EstanciaCamaEntity, Long> {

    List<EstanciaCamaEntity> findByEpisodio_IdOrderByFechaHoraInicioAsc(Long episodioId);

    Optional<EstanciaCamaEntity> findFirstByEpisodio_IdAndFechaHoraFinIsNullOrderByFechaHoraInicioDesc(
            Long episodioId);
}
