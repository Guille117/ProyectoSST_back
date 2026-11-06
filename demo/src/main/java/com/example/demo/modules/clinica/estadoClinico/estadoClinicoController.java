package com.example.demo.modules.clinica.estadoClinico;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/estadosClinico")
public class estadoClinicoController extends controllerBase<estadoClinicoEntity> {

    private final estadoClinicoService estadoClinicoService;

    public estadoClinicoController(estadoClinicoService estadoClinicoService) {
        this.estadoClinicoService = estadoClinicoService;
    }

    @Override
    protected estadoClinicoService getService() {
        return estadoClinicoService;
    }
}
