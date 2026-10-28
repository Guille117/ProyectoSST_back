package com.example.demo.modules.farmacia.proveedores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<ProveedorEntity, Long> {

    boolean existsByNit(String nit);

    boolean existsByNitIgnoreCase(String nit);

    Optional<ProveedorEntity> findByNitIgnoreCase(String nit);

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<ProveedorEntity> findByNombreIgnoreCase(String nombre);

    List<ProveedorEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ProveedorEntity> findByNombreContainingIgnoreCaseAndEstado(String nombre, boolean estado);

    List<ProveedorEntity> findByEstado(boolean estado);

    boolean existsByTelefono(String telefono);

    Optional<ProveedorEntity> findByTelefono(String telefono);

    boolean existsByEmailIgnoreCase(String email);

    Optional<ProveedorEntity> findByEmailIgnoreCase(String email);

    boolean existsByCodigo(String codigo);

    Optional<ProveedorEntity> findByNit(String nit);

    
}
