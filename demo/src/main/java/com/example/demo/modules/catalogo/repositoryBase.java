package com.example.demo.modules.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean 
public interface repositoryBase <T extends entityBase> extends JpaRepository<T, Long>{
    List<T> findByEstado(boolean estado);
    Optional<T> findByNombreIgnoreCase(String nombre);
}
