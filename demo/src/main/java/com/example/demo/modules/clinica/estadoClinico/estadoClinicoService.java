package com.example.demo.modules.clinica.estadoClinico;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class estadoClinicoService extends serviceBase<estadoClinicoEntity> {

    private final estadoClinicoRepository repository;

    public estadoClinicoService(estadoClinicoRepository repository) {
        this.repository = repository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<estadoClinicoEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarEstadosClinicos() {
        return repository.countByEstado(true);
    }
}
