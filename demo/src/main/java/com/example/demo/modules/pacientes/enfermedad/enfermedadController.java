package com.example.demo.modules.pacientes.enfermedad;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enfermedades")
public class enfermedadController extends controllerBase<enfermedadEntity> {

    private final enfermedadService service;

    public enfermedadController(enfermedadService service) {
        this.service = service;
    }

    @Override
    protected enfermedadService getService() {
        return service;
    }
}
