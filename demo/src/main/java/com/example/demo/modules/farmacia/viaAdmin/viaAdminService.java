package com.example.demo.modules.farmacia.viaAdmin;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import org.springframework.stereotype.Service;

@Service
public class viaAdminService extends serviceBase<viaAdminEntity> {
    private final viaAdminRepository viaAdminRepository;
    private final medicamentoLogRepository medicamentoLogRepository;

    public viaAdminService(viaAdminRepository viaAdminRepository,
                           medicamentoLogRepository medicamentoLogRepository) {
        this.viaAdminRepository = viaAdminRepository;
        this.medicamentoLogRepository = medicamentoLogRepository;
    }

    @Override
    protected repositoryBase<viaAdminEntity> getRepository() {
        return viaAdminRepository;
    }

    @Override
    protected boolean estaRelacionado(viaAdminEntity entidad) {
        return medicamentoLogRepository.existsByViaAdmin_Id(entidad.getId());
    }
}