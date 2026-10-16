package com.example.demo.modules.clinica.diagnostico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface diagnosticoRepository extends JpaRepository<diagnosticoEntity, Long> {

    List<diagnosticoEntity> findByEstadoOrderByFechaDesc(boolean estado);

    List<diagnosticoEntity> findByEpisodio_IdAndEstadoOrderByFechaDesc(Long episodioId, boolean estado);

    List<diagnosticoEntity> findByTipoDiagnosticoAndEstadoOrderByFechaDesc(TipoDiagnostico tipoDiagnostico, boolean estado);
}
