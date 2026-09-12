package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/unidadMedida")
public class unidadMedidaController extends controllerBase<unidadMedidaEntity> {
    private final unidadMedidaService unidadMedidaService;

    public unidadMedidaController(unidadMedidaService unidadMedidaService) {
        this.unidadMedidaService = unidadMedidaService;
    }

    @Override
    protected unidadMedidaService getService() {
        return unidadMedidaService;
    }
}