package com.example.demo.modules.pacientes.enfermedad;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class enfermedadService extends serviceBase<enfermedadEntity> {

    private final enfermedadRepository repository;

    public enfermedadService(enfermedadRepository repository) {
        this.repository = repository;
    }

    @Override
    protected repositoryBase<enfermedadEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }
}
