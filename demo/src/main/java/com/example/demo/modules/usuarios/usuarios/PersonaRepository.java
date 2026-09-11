package com.example.demo.modules.usuarios.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<PersonaEntity, Long> {
    boolean existsByCui(String cui);
    Optional<PersonaEntity> findByCui(String cui);
}
