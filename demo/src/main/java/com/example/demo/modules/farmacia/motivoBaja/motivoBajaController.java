package com.example.demo.modules.farmacia.motivoBaja;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/motivoBaja")
public class motivoBajaController extends controllerBase<motivoBajaEntity> {
    private final motivoBajaService motivoBajaService;

    public motivoBajaController(motivoBajaService motivoBajaService) {
        this.motivoBajaService = motivoBajaService;
    }

    @Override
    protected motivoBajaService getService() {
        return motivoBajaService;
    }

    @Override
    public ResponseEntity<motivoBajaEntity> crear(@RequestBody motivoBajaEntity entidad) {
        return super.crear(entidad);
    }

    @Override
    public ResponseEntity<motivoBajaEntity> actualizar(@PathVariable("id") Long id, @RequestBody motivoBajaEntity entidadDetalles) {
        return motivoBajaService.actualizar(id, entidadDetalles)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<motivoBajaEntity> cambiarEstado(@PathVariable("id") Long id) {
        return super.cambiarEstado(id);
    }
}
