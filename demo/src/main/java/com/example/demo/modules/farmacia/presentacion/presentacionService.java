package com.example.demo.modules.farmacia.presentacion;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import org.springframework.stereotype.Service;

@Service
public class presentacionService extends serviceBase<presentacionEntity> {
    private final presentacionRepository presentacionRepository;
    private final medicamentoLogRepository medicamentoLogRepository;

    public presentacionService(presentacionRepository presentacionRepository,
                              medicamentoLogRepository medicamentoLogRepository) {
        this.presentacionRepository = presentacionRepository;
        this.medicamentoLogRepository = medicamentoLogRepository;
    }

    @Override
    protected repositoryBase<presentacionEntity> getRepository() {
        return presentacionRepository;
    }

    @Override
    protected boolean estaRelacionado(presentacionEntity entidad) {
        return medicamentoLogRepository.existsByPresentacion_Id(entidad.getId());
    }
}