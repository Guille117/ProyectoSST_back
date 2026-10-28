package com.example.demo.modules.usuarios.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmoduloRepository extends JpaRepository<SubmoduloEntity, Long> {
    Optional<SubmoduloEntity> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
