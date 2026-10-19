package com.example.demo.modules.clinica.parteCuerpo;

import com.example.demo.modules.catalogo.controllerBase;
import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoService;
import com.example.demo.modules.clinica.procedimiento.procedimientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partesCuerpo")
public class parteCuerpoController extends controllerBase<parteCuerpoEntity> {
    private final parteCuerpoService parteCuerpoService;
    private final condicionEgresoService condicionEgresoService;
    private final procedimientoService procedimientoService;

    public record ConteoCatalogosResponse(long partesCuerpo, long condicionesEgreso, long procedimientos) {}

    // Constructor para inicializar los servicios de los catálogos de gestión clínica
    public parteCuerpoController(
            parteCuerpoService parteCuerpoService,
            condicionEgresoService condicionEgresoService,
            procedimientoService procedimientoService) {
        this.parteCuerpoService = parteCuerpoService;
        this.condicionEgresoService = condicionEgresoService;
        this.procedimientoService = procedimientoService;
    }

    @Override
    protected parteCuerpoService getService() {
        return parteCuerpoService;
    }

    // Endpoint para contar los registros de los catálogos de gestión clínica
    @GetMapping("/conteo")
    public ConteoCatalogosResponse contarCatalogos() {
        return new ConteoCatalogosResponse(
                parteCuerpoService.contarPartesCuerpo(),
                condicionEgresoService.contarCondicionesEgreso(),
                procedimientoService.contarProcedimientos());
    }
}
