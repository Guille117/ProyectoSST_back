package com.example.demo.modules.farmacia.viaAdmin;

import org.springframework.stereotype.Repository;
import com.example.demo.modules.catalogo.repositoryBase;
import java.util.Optional;

@Repository
public interface viaAdminRepository extends repositoryBase<viaAdminEntity> {
    Optional<viaAdminEntity> findByNombreIgnoreCase(String nombre);
}