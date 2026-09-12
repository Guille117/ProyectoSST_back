package com.example.demo.modules.farmacia.marca;

import org.springframework.stereotype.Repository;
import com.example.demo.modules.catalogo.repositoryBase;
import java.util.Optional;

@Repository
public interface marcaRepository extends repositoryBase<marcaEntity> {
    Optional<marcaEntity> findByNombreIgnoreCase(String nombre);
}