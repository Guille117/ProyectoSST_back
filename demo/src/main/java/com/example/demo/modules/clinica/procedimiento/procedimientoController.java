package com.example.demo.modules.clinica.procedimiento;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/procedimientos")
public class procedimientoController extends controllerBase<procedimientoEntity> {

    private final procedimientoService procedimientoService;

    public procedimientoController(procedimientoService procedimientoService) {
        this.procedimientoService = procedimientoService;
    }

    @Override
    protected procedimientoService getService() {
        return procedimientoService;
    }
}
