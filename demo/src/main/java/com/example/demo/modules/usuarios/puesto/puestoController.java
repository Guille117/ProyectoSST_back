package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.controllerBase;
import com.example.demo.modules.usuarios.especialidad.especialidadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/puestos")
public class puestoController extends controllerBase<puestoEntity> {
    private final puestoService puestoService;
    private final especialidadService especialidadService;

    public record ConteoCatalogosResponse(long puestos, long especialidades) {}

    // Constructor para inicializar el servicio
    public puestoController(puestoService puestoService, especialidadService especialidadService) {
        this.puestoService = puestoService;
        this.especialidadService = especialidadService;
    }

    @Override
    protected puestoService getService() {
        return puestoService;
    }

    // Endpoint para contar los puestos
    @GetMapping("/conteo")
    public ConteoCatalogosResponse contarCatalogos() {
        return new ConteoCatalogosResponse(
                puestoService.contarPuestos(),
                especialidadService.contarEspecialidades());
    }
}

