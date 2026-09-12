package com.example.demo.modules.farmacia.viaAdmin;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class viaAdminService extends serviceBase<viaAdminEntity> {
    private final viaAdminRepository viaAdminRepository;

    public viaAdminService(viaAdminRepository viaAdminRepository) {
        this.viaAdminRepository = viaAdminRepository;
    }

    @Override
    protected repositoryBase<viaAdminEntity> getRepository() {
        return viaAdminRepository;
    }
}