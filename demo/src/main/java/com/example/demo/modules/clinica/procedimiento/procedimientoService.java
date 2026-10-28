package com.example.demo.modules.clinica.procedimiento;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class procedimientoService extends serviceBase<procedimientoEntity> {

    private final procedimientoRepository repository;

    public procedimientoService(procedimientoRepository repository) {
        this.repository = repository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<procedimientoEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarProcedimientos() {
        return repository.countByEstado(true);
    }
}
