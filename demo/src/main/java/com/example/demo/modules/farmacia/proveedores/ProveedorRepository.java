package com.example.demo.modules.farmacia.proveedores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<ProveedorEntity, Long> {

    boolean existsByNit(String nit);

    boolean existsByCodigo(String codigo);

    Optional<ProveedorEntity> findByNit(String nit);
}
