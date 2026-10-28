package com.example.demo.modules.farmacia.unidadMedida;

import org.springframework.stereotype.Repository;
import com.example.demo.modules.catalogo.repositoryBase;
import java.util.Optional;

@Repository
public interface unidadMedidaRepository extends repositoryBase<unidadMedidaEntity> {
    Optional<unidadMedidaEntity> findByNombreIgnoreCase(String nombre);
}