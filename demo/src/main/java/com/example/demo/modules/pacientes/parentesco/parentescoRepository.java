package com.example.demo.modules.pacientes.parentesco;

import com.example.demo.modules.catalogo.repositoryBase;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface parentescoRepository extends repositoryBase<parentescoEntity> {
    List<parentescoEntity> findByNombreContainingIgnoreCase(String nombre);
}