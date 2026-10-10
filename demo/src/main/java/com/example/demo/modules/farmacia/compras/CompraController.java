package com.example.demo.modules.farmacia.compras;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return new ResponseEntity<>(service.crear(comprobante, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CompraDTOs.Response>> obtenerTodos(@RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerTodos(activos));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CompraDTOs.Response>> buscarPorCodigo(
            @RequestParam(name = "codigo") String codigo,
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.buscarPorCodigo(codigo, activos));
    }

    /** Resumen de compras: código, proveedor, fecha, cantidad total de productos y total. */
    @GetMapping("/resumen")
    public ResponseEntity<List<CompraDTOs.ResumenResponse>> obtenerResumen(
            @RequestParam(name = "activos", required = false) Boolean activos) {
        return ResponseEntity.ok(service.obtenerResumen(activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTOs.Response> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
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
        return ResponseEntity.ok(service.actualizar(id, comprobante, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable("id") Long id) {
        service.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }
}
