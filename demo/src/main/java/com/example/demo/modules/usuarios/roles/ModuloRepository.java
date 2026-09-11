package com.example.demo.modules.usuarios.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<ModuloEntity, Long> {
    Optional<ModuloEntity> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
