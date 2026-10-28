package com.example.demo.modules.pacientes.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoEpisodioRepository extends JpaRepository<DocumentoEpisodioEntity, Long> {

    List<DocumentoEpisodioEntity> findByEpisodio_IdAndEstadoOrderByFechaSubidaDesc(Long episodioId, boolean estado);

    List<DocumentoEpisodioEntity> findByEpisodio_IdAndTipoAndEstadoOrderByFechaSubidaDesc(
            Long episodioId, TipoDocumentoEpisodio tipo, boolean estado);
}
