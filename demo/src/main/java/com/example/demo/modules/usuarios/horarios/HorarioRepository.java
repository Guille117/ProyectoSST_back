package com.example.demo.modules.usuarios.horarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepository extends JpaRepository<HorarioEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<HorarioEntity> findByNombreIgnoreCase(String nombre);

    List<HorarioEntity> findByNombreContainingIgnoreCase(String nombre);

    List<HorarioEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<HorarioEntity> findByEstado(boolean estado);

    boolean existsByCodigo(String codigo);

    Optional<HorarioEntity> findByCodigo(String codigo);
}
