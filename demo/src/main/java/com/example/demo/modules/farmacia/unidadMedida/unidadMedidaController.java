package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.controllerBase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("@permissionService.canCreate(authentication, 'UNIDADMEDIDA') or @permissionService.canCreate(authentication, 'CATALOGOS')")
    @Override
    public ResponseEntity<unidadMedidaEntity> crear(@RequestBody unidadMedidaEntity entidad) {
        return super.crear(entidad);
    }

    @PreAuthorize("@permissionService.canEdit(authentication, 'UNIDADMEDIDA') or @permissionService.canEdit(authentication, 'CATALOGOS')")
    @Override
    public ResponseEntity<unidadMedidaEntity> actualizar(@PathVariable("id") Long id, @RequestBody unidadMedidaEntity entidadDetalles) {
        return super.actualizar(id, entidadDetalles);
    }

    @PreAuthorize("@permissionService.canEdit(authentication, 'UNIDADMEDIDA') or @permissionService.canEdit(authentication, 'CATALOGOS')")
    @Override
    public ResponseEntity<unidadMedidaEntity> cambiarEstado(@PathVariable("id") Long id) {
        return super.cambiarEstado(id);
    }
}