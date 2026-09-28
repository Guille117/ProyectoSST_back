package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Override
    public ResponseEntity<unidadMedidaEntity> crear(@RequestBody unidadMedidaEntity entidad) {
        return super.crear(entidad);
    }

    @Override
    public ResponseEntity<unidadMedidaEntity> actualizar(@PathVariable("id") Long id, @RequestBody unidadMedidaEntity entidadDetalles) {
        return super.actualizar(id, entidadDetalles);
    }

    @Override
    public ResponseEntity<unidadMedidaEntity> cambiarEstado(@PathVariable("id") Long id) {
        return super.cambiarEstado(id);
    }
}