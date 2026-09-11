package com.example.demo.modules.usuarios.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<RolEntity> findByNombreIgnoreCase(String nombre);

    List<RolEntity> findByNombreContainingIgnoreCase(String nombre);

    List<RolEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<RolEntity> findByEstado(boolean estado);

    boolean existsByCodigo(String codigo);

    Optional<RolEntity> findByCodigo(String codigo);

    List<RolEntity> findByCodigoContainingIgnoreCase(String criterio);

    List<RolEntity> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
}
