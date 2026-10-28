package com.example.demo.modules.usuarios.especialidad;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/especialidades")
public class especialidadController extends controllerBase<especialidadEntity> {

    private final especialidadService especialidadService;

    public especialidadController(especialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @Override
    protected especialidadService getService() {
        return especialidadService;
    }
}