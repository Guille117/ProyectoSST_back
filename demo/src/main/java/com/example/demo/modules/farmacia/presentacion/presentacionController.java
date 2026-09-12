package com.example.demo.modules.farmacia.presentacion;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/presentacion")
public class presentacionController extends controllerBase<presentacionEntity> {
    private final presentacionService presentacionService;

    public presentacionController(presentacionService presentacionService) {
        this.presentacionService = presentacionService;
    }

    @Override
    protected presentacionService getService() {
        return presentacionService;
    }
}