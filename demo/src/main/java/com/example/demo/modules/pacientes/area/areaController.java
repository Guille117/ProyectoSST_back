package com.example.demo.modules.pacientes.area;

import com.example.demo.modules.pacientes.habitaciones.habitacionesService;
import com.example.demo.modules.pacientes.tipoCama.tipoCamaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class areaController {

    private final areaService service;
    private final habitacionesService habitacionesService;
    private final tipoCamaService tipoCamaService;

    public record ConteoCatalogoResponse(String tabla, long total) {}

    @PostMapping
    @PreAuthorize("@permissionService.canCreate(authentication, 'AREAS') or @permissionService.canCreateCatalog(authentication)")
    public ResponseEntity<areaEntity> crear(@Valid @RequestBody areaEntity request) {
        request.setId(null);
        request.setEstado(true);
        return new ResponseEntity<>(service.guardar(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<areaEntity>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(activos == null ? service.listarTodos() : service.listarPorEstado(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<areaEntity>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<areaEntity> obtenerPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'AREAS') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<areaEntity> actualizar(@PathVariable("id") Long id, @RequestBody areaEntity request) {
        return ResponseEntity.ok(service.actualizarConDescripcion(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit(authentication, 'AREAS') or @permissionService.canEditCatalog(authentication)")
    public ResponseEntity<areaEntity> cambiarEstado(@PathVariable("id") Long id) {
        return service.cambiarEstado(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/conteo-catalogos")
    public ResponseEntity<List<ConteoCatalogoResponse>> contarCatalogosActivos() {
        return ResponseEntity.ok(List.of(
                new ConteoCatalogoResponse("areas", service.contarActivos()),
                new ConteoCatalogoResponse("habitaciones", habitacionesService.contarActivos()),
                new ConteoCatalogoResponse("tipos_cama", tipoCamaService.contarActivos())
        ));
    }
}