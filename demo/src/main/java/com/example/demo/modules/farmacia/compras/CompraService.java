package com.example.demo.modules.farmacia.compras;

import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.lotes.LoteDTOs;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.lotes.LoteService;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository repository;
    private final CompraMapper mapper;
    private final ComprobanteArchivoStorage comprobanteStorage;
    private final ProveedorRepository proveedorRepository;
    private final LoteService loteService;
    /**
     * Lectura directa de lotes para los endpoints de resumen y detalle.
     * Se usa el repositorio (y no LoteService) porque LoteDTOs.Response no expone los datos del
     * medicamento (dosis, unidad de medida, marca, presentación) ni el detalle del insumo.
     */
    private final LoteRepository loteRepository;

    @Transactional
    public CompraDTOs.Response crear(MultipartFile comprobante, CompraDTOs.Request req) {
        Long proveedorId = req == null ? null : req.proveedorId();
        ProveedorEntity proveedor = validarProveedorActivo(proveedorId);
        List<CompraDTOs.LoteEntrada> lotes = req == null ? null : req.lotes();
        validarLotes(lotes);

        boolean tieneComprobante = comprobante != null && !comprobante.isEmpty();

        CompraEntity entity = CompraEntity.builder()
                .codigo(generarCodigo())
                .total(BigDecimal.ZERO)
                .estado(true)
                .fecha(LocalDateTime.now())
                .proveedor(proveedor)
                .build();
        CompraEntity guardado = repository.save(entity);

        AtomicReference<String> rutaGuardada = new AtomicReference<>();
        try {
            if (tieneComprobante) {
                String ruta = comprobanteStorage.guardar(comprobante, guardado.getId());
                rutaGuardada.set(ruta);
                guardado.setComprobante(ruta);
            }
            CompraEntity finalCompra = repository.save(guardado);

            List<LoteDTOs.Response> lotesCreados = new ArrayList<>();
            for (CompraDTOs.LoteEntrada entrada : lotes) {
                lotesCreados.add(loteService.crear(convertirLote(entrada, finalCompra.getId())));
            }

            CompraDTOs.Response base = mapper.toDTO(finalCompra);
            return new CompraDTOs.Response(
                    base.id(), base.codigo(), base.total(), base.comprobante(), base.estado(),
                    base.proveedorId(), base.proveedorNombre(), base.fecha(), lotesCreados);
        } catch (RuntimeException ex) {
            try {
                comprobanteStorage.eliminar(rutaGuardada.get());
            } catch (RuntimeException limpieza) {
                ex.addSuppressed(limpieza);
            }
            throw ex;
        }
    }

    @Transactional
    public CompraDTOs.Response actualizar(Long id, MultipartFile comprobante, CompraDTOs.Request req) {
        CompraEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));

        if (req != null && req.proveedorId() != null) {
            existente.setProveedor(validarProveedorActivo(req.proveedorId()));
        }

        AtomicReference<String> rutaNueva = new AtomicReference<>();
        try {
            if (comprobante != null && !comprobante.isEmpty()) {
                String ruta = comprobanteStorage.guardar(comprobante, existente.getId());
                rutaNueva.set(ruta);
                String rutaAnterior = existente.getComprobante();
                existente.setComprobante(ruta);
                CompraEntity actualizado = repository.save(existente);
                comprobanteStorage.eliminar(rutaAnterior);
                return mapper.toDTO(actualizado);
            }
            CompraEntity actualizado = repository.save(existente);
            return mapper.toDTO(actualizado);
        } catch (RuntimeException ex) {
            try {
                comprobanteStorage.eliminar(rutaNueva.get());
            } catch (RuntimeException limpieza) {
                ex.addSuppressed(limpieza);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public CompraDTOs.Response obtenerPorId(Long id) {
        CompraEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream().map(mapper::toDTO).toList();
        }
        return repository.findByEstado(activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.Response> buscarPorCodigo(String codigo, Boolean activos) {
        if (codigo == null || codigo.isBlank()) {
            return List.of();
        }
        String norm = codigo.trim();
        if (activos == null) {
            return repository.findByCodigoContainingIgnoreCase(norm).stream().map(mapper::toDTO).toList();
        }
        return repository.findByCodigoContainingIgnoreCaseAndEstado(norm, activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        CompraEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.ResumenResponse> obtenerResumen(Boolean activos) {
        List<CompraEntity> compras = activos == null
                ? repository.findAll()
                : repository.findByEstado(activos);
        Map<Long, Integer> cantidades = cantidadesPorCompra(compras);
        return compras.stream()
                .map(compra -> new CompraDTOs.ResumenResponse(
                        compra.getId(),
                        compra.getCodigo(),
                        nombreProveedor(compra),
                        compra.getFecha(),
                        cantidades.getOrDefault(compra.getId(), 0),
                        compra.getTotal()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompraDTOs.DetalleResponse obtenerDetalle(Long id) {
        CompraEntity compra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        List<CompraDTOs.LoteDetalleResponse> lotes = loteRepository.findByCompra_Id(id).stream()
                .map(this::toLoteDetalle)
                .toList();
        return new CompraDTOs.DetalleResponse(
                compra.getId(),
                compra.getCodigo(),
                idProveedor(compra),
                nombreProveedor(compra),
                compra.getFecha(),
                compra.getTotal(),
                lotes);
    }

    /** Suma la cantidad de todos los lotes (insumos y medicamentos) agrupada por compra. */
    private Map<Long, Integer> cantidadesPorCompra(List<CompraEntity> compras) {
        List<Long> ids = compras.stream().map(CompraEntity::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return loteRepository.findByCompra_IdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        lote -> lote.getCompra().getId(),
                        Collectors.summingInt(lote -> lote.getCantidad() == null ? 0 : lote.getCantidad())));
    }

    /** Arma el lote del detalle con los datos del medicamento (si aplica) o del insumo. */
    private CompraDTOs.LoteDetalleResponse toLoteDetalle(LoteEntity lote) {
        medicamentoLogEntity medicamento = lote.getMedicamentoLog();
        if (medicamento != null) {
            return new CompraDTOs.LoteDetalleResponse(
                    lote.getId(),
                    lote.getCodigoLote(),
                    medicamento.getCodigo(),
                    true,
                    medicamento.getNombre(),
                    medicamento.getDosis(),
                    medicamento.getUnidadMedida() == null ? null : medicamento.getUnidadMedida().getAbreviatura(),
                    medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre(),
                    medicamento.getPresentacion() == null ? null : medicamento.getPresentacion().getNombre(),
                    null,
                    lote.getCantidad(),
                    lote.getPrecioCompra(),
                    lote.getFechaVencimiento());
        }
        insumosLogEntity insumo = lote.getInsumoLog();
        return new CompraDTOs.LoteDetalleResponse(
                lote.getId(),
                lote.getCodigoLote(),
                insumo == null ? null : insumo.getCodigo(),
                false,
                insumo == null ? null : insumo.getNombre(),
                null,
                null,
                insumo == null || insumo.getMarca() == null ? null : insumo.getMarca().getNombre(),
                null,
                insumo == null ? null : insumo.getDetalle(),
                lote.getCantidad(),
                lote.getPrecioCompra(),
                lote.getFechaVencimiento());
    }

    private Long idProveedor(CompraEntity compra) {
        return compra.getProveedor() == null ? null : compra.getProveedor().getId();
    }

    private String nombreProveedor(CompraEntity compra) {
        return compra.getProveedor() == null ? null : compra.getProveedor().getNombre();
    }

    private String generarCodigo() {
        long total = repository.count();
        return String.format("COM-%d", total + 1);
    }

    private ProveedorEntity validarProveedorActivo(Long proveedorId) {
        if (proveedorId == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        ProveedorEntity proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con el ID: " + proveedorId));
        if (!proveedor.isEstado()) {
            throw new IllegalArgumentException("El proveedor está inactivo. Actívalo primero para asociarlo a una compra");
        }
        return proveedor;
    }

    private void validarLotes(List<CompraDTOs.LoteEntrada> lotes) {
        if (lotes == null || lotes.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un lote");
        }
        Set<String> codigosVistos = new HashSet<>();
        for (CompraDTOs.LoteEntrada entrada : lotes) {
            if (entrada == null) {
                throw new IllegalArgumentException("La lista de lotes contiene entradas vacías");
            }
            if (entrada.idItem() == null) {
                throw new IllegalArgumentException("El id del item del lote es obligatorio");
            }
            String codigo = StringNormalizer.normalizarTexto(entrada.codigoLote());
            if (codigo.isBlank()) {
                throw new IllegalArgumentException("El código del lote es obligatorio");
            }
            if (!codigosVistos.add(codigo.toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("El código de lote " + codigo + " está duplicado en la solicitud");
            }
            if (entrada.cantidad() == null || entrada.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad del lote debe ser mayor a 0");
            }
            if (entrada.esMedicamento() && entrada.fechaVencimiento() == null) {
                throw new IllegalArgumentException("La fecha de vencimiento del lote es obligatoria para medicamentos");
            }
            if (entrada.precioCompra() == null || entrada.precioCompra().signum() <= 0) {
                throw new IllegalArgumentException("El precio de compra del lote debe ser mayor a 0");
            }
            if (entrada.precioVenta() == null || entrada.precioVenta().signum() <= 0) {
                throw new IllegalArgumentException("El precio de venta del lote debe ser mayor a 0");
            }
        }
    }

    private LoteDTOs.Request convertirLote(CompraDTOs.LoteEntrada entrada, Long idCompra) {
        Long idInsumoLog = entrada.esMedicamento() ? null : entrada.idItem();
        Long idMedicamentoLog = entrada.esMedicamento() ? entrada.idItem() : null;
        return new LoteDTOs.Request(
                idCompra,
                idInsumoLog,
                idMedicamentoLog,
                StringNormalizer.normalizarTexto(entrada.codigoLote()),
                entrada.cantidad(),
                entrada.fechaVencimiento(),
                entrada.precioCompra(),
                entrada.precioVenta());
    }
}
