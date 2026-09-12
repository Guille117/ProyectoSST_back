package com.example.demo.modules.farmacia.presentacion;

import org.springframework.stereotype.Repository;
import com.example.demo.modules.catalogo.repositoryBase;
import java.util.Optional;

@Repository
public interface presentacionRepository extends repositoryBase<presentacionEntity> {
    Optional<presentacionEntity> findByNombreIgnoreCase(String nombre);
}