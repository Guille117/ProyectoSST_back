package com.example.demo.modules.catalogos.catalogos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoRepository extends JpaRepository<CatalogoEntity, Long> {

    Optional<CatalogoEntity> findByNombreIgnoreCase(String nombre);

    List<CatalogoEntity> findByNombreContainingIgnoreCase(String nombre);

    List<CatalogoEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<CatalogoEntity> findByEstado(boolean estado);

    Optional<CatalogoEntity> findByCodigo(String codigo);
}
