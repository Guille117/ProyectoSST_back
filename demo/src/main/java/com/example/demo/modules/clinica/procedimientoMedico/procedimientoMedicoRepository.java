package com.example.demo.modules.clinica.procedimientoMedico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface procedimientoMedicoRepository extends JpaRepository<procedimientoMedicoEntity, Long> {

    List<procedimientoMedicoEntity> findByEpisodio_IdOrderByInicioDesc(Long episodioId);
}
