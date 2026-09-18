package com.example.demo.modules.usuarios.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    boolean existsByUsernameIgnoreCase(String username);
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);

    boolean existsByCodigo(String codigo);
    Optional<UsuarioEntity> findByCodigo(String codigo);

    List<UsuarioEntity> findByEstado(boolean estado);

    List<UsuarioEntity> findByUsernameContainingIgnoreCase(String username);
    List<UsuarioEntity> findByCodigoContainingIgnoreCase(String codigo);

    boolean existsByRoles_Id(Long rolId);

    boolean existsByHorario_Id(Long horarioId);
}
