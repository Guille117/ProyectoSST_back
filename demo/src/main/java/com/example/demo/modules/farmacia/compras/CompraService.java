package com.example.demo.modules.farmacia.compras;

import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.lotes.LoteDTOs;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.lotes.LoteService;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final SolicitudSalidaMovimientoRepository solicitudSalidaMovimientoRepository;

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

            for (CompraDTOs.LoteEntrada entrada : lotes) {
                loteService.crear(convertirLote(entrada, finalCompra.getId()));
            }

            return toResponse(finalCompra, loteRepository.findByCompra_Id(finalCompra.getId()));
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
        if (req != null && req.lotes() != null) {
            actualizarLotes(existente.getId(), req.lotes());
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
                return toResponse(actualizado, loteRepository.findByCompra_Id(actualizado.getId()));
            }
            CompraEntity actualizado = repository.save(existente);
            return toResponse(actualizado, loteRepository.findByCompra_Id(actualizado.getId()));
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
        return toResponse(entity, loteRepository.findByCompra_Id(entity.getId()));
    }

    @Transactional(readOnly = true)
    public ComprobanteArchivoStorage.Archivo obtenerComprobante(Long id) {
        CompraEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        return comprobanteStorage.cargar(entity.getComprobante());
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.Response> obtenerTodos(Boolean activos) {
        List<CompraEntity> compras = activos == null
                ? repository.findAll()
                : repository.findByEstado(activos);
        return toResponses(compras);
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.Response> buscarPorCodigo(String codigo, Boolean activos) {
        if (codigo == null || codigo.isBlank()) {
            return List.of();
        }
        String norm = codigo.trim();
        List<CompraEntity> compras = activos == null
                ? repository.findByCodigoContainingIgnoreCase(norm)
                : repository.findByCodigoContainingIgnoreCaseAndEstado(norm, activos);
        return toResponses(compras);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        CompraEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        validarCompraSinUso(entity, "cambiar el estado de");
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    @Transactional
    public void eliminar(Long id) {
        CompraEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));
        validarCompraSinUso(entity, "eliminar");
        comprobanteStorage.eliminar(entity.getComprobante());
        loteRepository.deleteAll(loteRepository.findByCompra_Id(id));
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<CompraDTOs.ResumenResponse> obtenerResumen(Boolean activos) {
        return toResumen(comprasSegunEstado(activos));
    }

    /**
     * Busca compras y devuelve su resumen. Todos los filtros son opcionales y se combinan entre sí:
     * código (contiene, sin distinguir mayúsculas), proveedor y fecha (día exacto).
     */
    @Transactional(readOnly = true)
    public List<CompraDTOs.ResumenResponse> buscarResumen(
            String codigo, Long proveedorId, LocalDate fecha, Boolean activos) {
        String codigoNormalizado = codigo == null ? null : codigo.trim().toLowerCase(Locale.ROOT);
        List<CompraEntity> compras = comprasSegunEstado(activos).stream()
                .filter(compra -> codigoNormalizado == null || codigoNormalizado.isBlank()
                        || (compra.getCodigo() != null
                                && compra.getCodigo().toLowerCase(Locale.ROOT).contains(codigoNormalizado)))
                .filter(compra -> proveedorId == null
                        || (compra.getProveedor() != null && proveedorId.equals(compra.getProveedor().getId())))
                .filter(compra -> fecha == null
                        || (compra.getFecha() != null && fecha.equals(compra.getFecha().toLocalDate())))
                .toList();
        return toResumen(compras);
    }

    private List<CompraEntity> comprasSegunEstado(Boolean activos) {
        return activos == null ? repository.findAll() : repository.findByEstado(activos);
    }

    private List<CompraDTOs.ResumenResponse> toResumen(List<CompraEntity> compras) {
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

    private List<CompraDTOs.Response> toResponses(List<CompraEntity> compras) {
        List<Long> ids = compras.stream().map(CompraEntity::getId).filter(Objects::nonNull).toList();
        Map<Long, List<LoteEntity>> lotesPorCompra = ids.isEmpty()
                ? Collections.emptyMap()
                : loteRepository.findByCompra_IdIn(ids).stream()
                        .collect(Collectors.groupingBy(lote -> lote.getCompra().getId()));
        return compras.stream()
                .map(compra -> toResponse(compra, lotesPorCompra.getOrDefault(compra.getId(), List.of())))
                .toList();
    }

    private CompraDTOs.Response toResponse(CompraEntity compra, List<LoteEntity> lotes) {
        CompraDTOs.Response base = mapper.toDTO(compra);
        List<CompraDTOs.LoteResponse> lotesResponse = lotes == null
                ? List.of()
                : lotes.stream().map(this::toLoteResponse).toList();
        return new CompraDTOs.Response(
                base.id(), base.codigo(), base.total(), base.comprobante(), base.estado(),
                base.proveedorId(), base.proveedorNombre(), base.fecha(), lotesResponse);
    }

    private CompraDTOs.LoteResponse toLoteResponse(LoteEntity lote) {
        medicamentoLogEntity medicamento = lote.getMedicamentoLog();
        insumosLogEntity insumo = lote.getInsumoLog();
        return new CompraDTOs.LoteResponse(
                lote.getId(),
                lote.getCodigoLote(),
                lote.getCantidad(),
                lote.getDisponible(),
                lote.isEstado(),
                lote.getFechaVencimiento(),
                lote.getPrecioCompra(),
                lote.getPrecioVenta(),
                lote.getTotal(),
                insumo == null ? null : insumo.getId(),
                insumo == null ? null : insumo.getNombre(),
                medicamento == null ? null : medicamento.getId(),
                medicamento == null ? null : nombreMedicamento(medicamento),
                lote.getCompra() == null ? null : lote.getCompra().getId(),
                lote.getCompra() == null ? null : lote.getCompra().getCodigo());
    }

    /** Nombre de medicamento como texto: nombre, fabricante, presentacion, dosis y unidad. */
    private String nombreMedicamento(medicamentoLogEntity medicamento) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, medicamento.getNombre());
        agregarParte(partes, medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre());
        agregarParte(partes, medicamento.getPresentacion() == null ? null : medicamento.getPresentacion().getNombre());
        String dosisUnidad = dosisConUnidad(medicamento);
        if (dosisUnidad != null) {
            partes.add(dosisUnidad);
        }
        return partes.isEmpty() ? null : String.join(" ", partes);
    }

    private String dosisConUnidad(medicamentoLogEntity medicamento) {
        if (medicamento.getDosis() == null) {
            return null;
        }
        String dosis = medicamento.getDosis().stripTrailingZeros().toPlainString();
        String unidad = medicamento.getUnidadMedida() == null ? null : medicamento.getUnidadMedida().getAbreviatura();
        return unidad == null || unidad.isBlank() ? dosis : dosis + unidad.trim();
    }

    private void agregarParte(List<String> partes, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(valor.trim());
        }
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

    /** Sincroniza los lotes enviados con los almacenados: crea, actualiza y elimina los faltantes. */
    private void actualizarLotes(Long compraId, List<CompraDTOs.LoteEntrada> entradas) {
        validarLotes(entradas);
        List<LoteEntity> existentes = loteRepository.findByCompra_Id(compraId);
        Map<String, LoteEntity> porCodigo = existentes.stream()
                .collect(Collectors.toMap(
                        lote -> lote.getCodigoLote().toUpperCase(Locale.ROOT),
                        lote -> lote,
                        (primero, segundo) -> primero));

        Set<String> codigosRecibidos = new HashSet<>();
        for (CompraDTOs.LoteEntrada entrada : entradas) {
            String codigo = StringNormalizer.normalizarTexto(entrada.codigoLote()).toUpperCase(Locale.ROOT);
            codigosRecibidos.add(codigo);
            LoteEntity existente = porCodigo.get(codigo);
            if (existente == null) {
                loteService.crear(convertirLote(entrada, compraId));
            } else {
                loteService.actualizar(existente.getId(), convertirLote(entrada, compraId));
            }
        }

        for (LoteEntity lote : existentes) {
            if (!codigosRecibidos.contains(lote.getCodigoLote().toUpperCase(Locale.ROOT))) {
                loteService.eliminar(lote.getId());
            }
        }
    }

    /** Bloquea eliminar o cambiar el estado de una compra cuyos lotes ya registran unidades usadas o despachadas. */
    private void validarCompraSinUso(CompraEntity compra, String accion) {
        boolean tieneStockUsado = loteRepository.findByCompra_Id(compra.getId()).stream()
                .anyMatch(lote -> lote.getDisponible() != null
                        && lote.getCantidad() != null
                        && lote.getDisponible() < lote.getCantidad());
        boolean tieneLotesDespachados =
                solicitudSalidaMovimientoRepository.existsByLote_Compra_Id(compra.getId());

        if (tieneStockUsado || tieneLotesDespachados) {
            throw new IllegalArgumentException(
                    "No se puede " + accion + " la compra porque ya se han usado productos de sus lotes");
        }
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
