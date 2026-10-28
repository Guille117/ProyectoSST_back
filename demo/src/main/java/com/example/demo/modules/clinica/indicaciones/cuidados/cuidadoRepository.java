package com.example.demo.modules.clinica.indicaciones.cuidados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface cuidadoRepository extends JpaRepository<cuidadoEntity, Long> {

    List<cuidadoEntity> findByEstado(EstadoCuidado estado);

    List<cuidadoEntity> findByEpisodio_IdAndEstado(Long episodioId, EstadoCuidado estado);
}
