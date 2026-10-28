package com.example.demo.modules.clinica.condicionEgreso;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/condicionesEgreso")
public class condicionEgresoController extends controllerBase<condicionEgresoEntity> {

    private final condicionEgresoService condicionEgresoService;

    public condicionEgresoController(condicionEgresoService condicionEgresoService) {
        this.condicionEgresoService = condicionEgresoService;
    }

    @Override
    protected condicionEgresoService getService() {
        return condicionEgresoService;
    }
}
