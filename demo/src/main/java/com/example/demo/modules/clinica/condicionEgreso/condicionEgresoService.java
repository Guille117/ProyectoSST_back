package com.example.demo.modules.clinica.condicionEgreso;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class condicionEgresoService extends serviceBase<condicionEgresoEntity> {

    private final condicionEgresoRepository repository;

    public condicionEgresoService(condicionEgresoRepository repository) {
        this.repository = repository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<condicionEgresoEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarCondicionesEgreso() {
        return repository.countByEstado(true);
    }
}
