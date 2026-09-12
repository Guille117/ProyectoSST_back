package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class puestoService extends serviceBase<puestoEntity>{

    // crea una variable inmutable
    private final puestoRepository puestoRepository;

    // Constructor para inicializar el repositorio
    public puestoService(puestoRepository puestoRepository) {
        this.puestoRepository = puestoRepository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<puestoEntity> getRepository() {
        return puestoRepository;
    }
    
}
