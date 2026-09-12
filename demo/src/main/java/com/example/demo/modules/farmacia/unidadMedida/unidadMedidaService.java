package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class unidadMedidaService extends serviceBase<unidadMedidaEntity> {
    private final unidadMedidaRepository unidadMedidaRepository;

    public unidadMedidaService(unidadMedidaRepository unidadMedidaRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    @Override
    protected repositoryBase<unidadMedidaEntity> getRepository() {
        return unidadMedidaRepository;
    }
}