package com.example.demo.modules.pacientes.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaResponsableRepository extends JpaRepository<PersonaResponsableEntity, Long> {
}