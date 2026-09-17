package com.example.demo.modules.usuarios.puesto;

import org.springframework.stereotype.Repository;
import com.example.demo.modules.catalogo.repositoryBase;
import java.util.Optional;

@Repository
public interface puestoRepository extends repositoryBase<puestoEntity> {
    Optional<puestoEntity> findByNombreIgnoreCase(String nombre);
    Integer countByEstado(Boolean estado);
}
