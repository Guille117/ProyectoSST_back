package com.example.demo.modules.farmacia.marca;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marca")
public class marcaController extends controllerBase<marcaEntity> {
    private final marcaService marcaService;

    public marcaController(marcaService marcaService) {
        this.marcaService = marcaService;
    }

    @Override
    protected marcaService getService() {
        return marcaService;
    }
}