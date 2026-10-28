package com.example.demo.modules.usuarios.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    boolean existsByUsernameIgnoreCase(String username);
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);

    boolean existsByCodigo(String codigo);
    Optional<UsuarioEntity> findByCodigo(String codigo);

    List<UsuarioEntity> findByEstado(boolean estado);

    @Query("select u from UsuarioEntity u join u.puesto p where u.estado = true and lower(trim(p.nombre)) in :nombres")
    List<UsuarioEntity> findMedicosActivos(@Param("nombres") Collection<String> nombres);

    List<UsuarioEntity> findByUsernameContainingIgnoreCase(String username);
    List<UsuarioEntity> findByCodigoContainingIgnoreCase(String codigo);

    boolean existsByRoles_Id(Long rolId);

    boolean existsByHorario_Id(Long horarioId);

    boolean existsByPuesto_Id(Long puestoId);

    boolean existsByEspecialidad_Id(Long especialidadId);
}
