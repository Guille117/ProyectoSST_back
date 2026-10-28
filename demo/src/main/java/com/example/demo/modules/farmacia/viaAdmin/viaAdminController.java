package com.example.demo.modules.farmacia.viaAdmin;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/viaAdmin")
public class viaAdminController extends controllerBase<viaAdminEntity> {
    private final viaAdminService viaAdminService;

    public viaAdminController(viaAdminService viaAdminService) {
        this.viaAdminService = viaAdminService;
    }

    @Override
    protected viaAdminService getService() {
        return viaAdminService;
    }
}