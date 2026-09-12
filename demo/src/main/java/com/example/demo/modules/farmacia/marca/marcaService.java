package com.example.demo.modules.farmacia.marca;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class marcaService extends serviceBase<marcaEntity> {
    private final marcaRepository marcaRepository;

    public marcaService(marcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Override
    protected repositoryBase<marcaEntity> getRepository() {
        return marcaRepository;
    }
}