package com.example.demo.modules.bitacora;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface BitacoraRepository extends JpaRepository<BitacoraEntity, Long> {

    @Query("""
            SELECT b FROM BitacoraEntity b
            WHERE (:tipos IS NULL OR b.tipo IN :tipos)
              AND (:pacienteId IS NULL OR b.pacienteId = :pacienteId)
              AND (:usuarioId IS NULL OR b.usuarioId = :usuarioId)
              AND (:desde IS NULL OR b.fechaHora >= :desde)
              AND (:hasta IS NULL OR b.fechaHora <= :hasta)
            ORDER BY b.fechaHora DESC
            """)
    Page<BitacoraEntity> filtrar(
            @Param("tipos") Collection<TipoBitacora> tipos,
            @Param("pacienteId") Long pacienteId,
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);
}
