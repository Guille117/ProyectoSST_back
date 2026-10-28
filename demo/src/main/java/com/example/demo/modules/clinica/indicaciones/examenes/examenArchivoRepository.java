package com.example.demo.modules.clinica.indicaciones.examenes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface examenArchivoRepository extends JpaRepository<examenArchivoEntity, Long> {
}
