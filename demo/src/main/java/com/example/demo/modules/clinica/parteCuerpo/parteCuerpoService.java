package com.example.demo.modules.clinica.parteCuerpo;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class parteCuerpoService extends serviceBase<parteCuerpoEntity> {

    private final parteCuerpoRepository repository;

    public parteCuerpoService(parteCuerpoRepository repository) {
        this.repository = repository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<parteCuerpoEntity> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public long contarPartesCuerpo() {
        return repository.countByEstado(true);
    }
}
