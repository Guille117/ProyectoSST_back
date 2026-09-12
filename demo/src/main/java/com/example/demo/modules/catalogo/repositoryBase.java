package com.example.demo.modules.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;

@NoRepositoryBean 
public interface repositoryBase <T extends entityBase> extends JpaRepository<T, Long>{
    List<T> findByEstado(boolean estado);
}
