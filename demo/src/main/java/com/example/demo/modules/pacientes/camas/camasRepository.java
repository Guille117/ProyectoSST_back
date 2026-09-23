package com.example.demo.modules.pacientes.camas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface camasRepository extends JpaRepository<camasEntity, Long> {
    boolean existsByCodigo(String codigo);
    List<camasEntity> findByActivo(boolean activo);

        @Query("""
                        SELECT c FROM camasEntity c
                        WHERE (:activos IS NULL OR c.activo = :activos)
                            AND (
                                        LOWER(c.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                                        OR LOWER(c.tipo.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                                        OR LOWER(c.habitacion.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                                        OR LOWER(c.area.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                                        OR c.estado = :estado
                                    )
                        """)
        List<camasEntity> buscar(@Param("texto") String texto,
                                 @Param("activos") Boolean activos,
                                 @Param("estado") EstadoCama estado);
}