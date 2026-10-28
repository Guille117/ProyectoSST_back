package com.example.demo.modules.pacientes.alergia;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alergias")
public class alergiaController extends controllerBase<alergiaEntity> {

    private final alergiaService service;

    public alergiaController(alergiaService service) {
        this.service = service;
    }

    @Override
    protected alergiaService getService() {
        return service;
    }
}
