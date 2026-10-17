package com.example.demo.modules.farmacia.compras;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompraDTOs.Response> crear(
            @RequestPart(value = "request", required = false) CompraDTOs.Request request,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        return new ResponseEntity<>(conComprobanteUrl(service.crear(comprobante, request)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CompraDTOs.Response>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(conComprobanteUrl(service.obtenerTodos(activos)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CompraDTOs.Response>> buscarPorCodigo(
            @RequestParam(name = "codigo") String codigo,
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(conComprobanteUrl(service.buscarPorCodigo(codigo, activos)));
    }

    /** Resumen de compras: código, proveedor, fecha, cantidad total de productos y total. */
    @GetMapping("/resumen")
    public ResponseEntity<List<CompraDTOs.ResumenResponse>> obtenerResumen(
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerResumen(activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(conComprobanteUrl(service.obtenerPorId(id)));
    }

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<Resource> obtenerComprobante(@PathVariable("id") Long id) {
        ComprobanteArchivoStorage.Archivo archivo = service.obtenerComprobante(id);
        return ResponseEntity.ok()
                .contentType(archivo.tipo())
                .body(archivo.recurso());
    }

    /** Detalle de una compra: cabecera + lotes con los datos del medicamento o del insumo. */
    @GetMapping("/{id}/detalle")
    public ResponseEntity<CompraDTOs.DetalleResponse> obtenerDetalle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompraDTOs.Response> actualizar(
            @PathVariable("id") Long id,
            @RequestPart(value = "request", required = false) CompraDTOs.Request request,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        return ResponseEntity.ok(conComprobanteUrl(service.actualizar(id, comprobante, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }

    /** Expone la ruta absoluta del comprobante para que el front pueda abrirlo directamente. */
    private CompraDTOs.Response conComprobanteUrl(CompraDTOs.Response response) {
        if (response == null || response.comprobante() == null) {
            return response;
        }
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/compras/{id}/comprobante")
                .buildAndExpand(response.id())
                .toUriString();
        return new CompraDTOs.Response(
                response.id(), response.codigo(), response.total(), url, response.estado(),
                response.proveedorId(), response.proveedorNombre(), response.fecha(), response.lotes());
    }

    private List<CompraDTOs.Response> conComprobanteUrl(List<CompraDTOs.Response> responses) {
        return responses == null ? null : responses.stream().map(this::conComprobanteUrl).toList();
    }
}
