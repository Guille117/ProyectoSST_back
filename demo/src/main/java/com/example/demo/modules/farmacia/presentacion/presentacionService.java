package com.example.demo.modules.farmacia.presentacion;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class presentacionService extends serviceBase<presentacionEntity> {
    private final presentacionRepository presentacionRepository;

    public presentacionService(presentacionRepository presentacionRepository) {
        this.presentacionRepository = presentacionRepository;
    }

    @Override
    protected repositoryBase<presentacionEntity> getRepository() {
        return presentacionRepository;
    }
}