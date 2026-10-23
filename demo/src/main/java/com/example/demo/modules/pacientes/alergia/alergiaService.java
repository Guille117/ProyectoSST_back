package com.example.demo.modules.pacientes.alergia;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class alergiaService extends serviceBase<alergiaEntity> {

    private final alergiaRepository repository;

    public alergiaService(alergiaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<alergiaEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }
}
