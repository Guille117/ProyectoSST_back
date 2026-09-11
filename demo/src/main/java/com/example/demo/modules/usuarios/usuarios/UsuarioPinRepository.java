package com.example.demo.modules.usuarios.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioPinRepository extends JpaRepository<UsuarioPinEntity, Long> {

    Optional<UsuarioPinEntity> findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(Long usuarioId);
}
