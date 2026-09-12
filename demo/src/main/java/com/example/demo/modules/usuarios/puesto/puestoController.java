package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/puestos")
public class puestoController extends controllerBase<puestoEntity> {
    private final puestoService puestoService;

    // Constructor para inicializar el servicio
    public puestoController(puestoService puestoService) {
        this.puestoService = puestoService;
    }

    @Override
    protected puestoService getService() {
        return puestoService;
    }

}

