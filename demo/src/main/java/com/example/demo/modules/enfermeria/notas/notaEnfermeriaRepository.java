package com.example.demo.modules.enfermeria.notas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface notaEnfermeriaRepository extends JpaRepository<notaEnfermeriaEntity, Long> {

    List<notaEnfermeriaEntity> findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(Long episodioId);
}
