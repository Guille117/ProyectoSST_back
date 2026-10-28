package com.example.demo.modules.farmacia.lotes;

import com.example.demo.modules.farmacia.compras.CompraEntity;
import com.example.demo.modules.farmacia.compras.CompraRepository;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoteService {

    private static final int STOCK_MINIMO = 20;
    private static final int DIAS_POR_VENCER_DEFAULT = 30;
    private static final Set<String> ESTADOS_INVENTARIO =
            Set.of("vencidos", "por-vencer", "stock-bajo", "sin-stock", "eliminados");

    private final LoteRepository repository;
    private final LoteMapper mapper;
    private final CompraRepository compraRepository;
    private final insumosLogRepository insumosLogRepository;
    private final medicamentoLogRepository medicamentoLogRepository;

    @Transactional
    public LoteDTOs.Response crear(LoteDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        validarTipoItem(req);
        validarFechaVencimiento(req);
        CompraEntity compra = obtenerCompra(req.idCompra());
        String codigoLote = normalizarCodigo(req.codigoLote());
        validarNoDuplicadoEnCompra(null, codigoLote, compra.getId());
        validarFechaConsistente(codigoLote, req.fechaVencimiento(), null);

        LoteEntity entity = mapper.toEntity(req);
        entity.setCodigoLote(codigoLote);
        entity.setCantidad(req.cantidad());
        entity.setDisponible(req.cantidad());
        entity.setEstado(true);
        entity.setFechaVencimiento(req.fechaVencimiento());
        entity.setPrecioCompra(req.precioCompra());
        entity.setPrecioVenta(req.precioVenta());
        entity.setTotal(calcularTotal(req.cantidad(), req.precioCompra()));
        entity.setInsumoLog(resolverInsumo(req.idInsumoLog()));
        entity.setMedicamentoLog(resolverMedicamento(req.idMedicamentoLog()));
        entity.setCompra(compra);

        LoteEntity guardado = repository.save(entity);
        recalcularTotalCompra(compra);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public LoteDTOs.Response actualizar(Long id, LoteDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        validarTipoItem(req);
        validarFechaVencimiento(req);
        LoteEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con el ID: " + id));
        CompraEntity compra = obtenerCompra(req.idCompra());
        String codigoLote = normalizarCodigo(req.codigoLote());
        validarNoDuplicadoEnCompra(id, codigoLote, compra.getId());
        validarFechaConsistente(codigoLote, req.fechaVencimiento(), id);

        CompraEntity compraAnterior = existente.getCompra();
        int unidadesUsadas = existente.getCantidad() - existente.getDisponible();
        if (req.cantidad() < unidadesUsadas) {
            int reduccion = existente.getCantidad() - req.cantidad();
            throw new IllegalArgumentException(
                    "No se puede reducir el lote " + codigoLote + ": la cantidad a reducir (" + reduccion
                            + ") supera la cantidad disponible (" + existente.getDisponible() + ")");
        }

        existente.setCodigoLote(codigoLote);
        existente.setCantidad(req.cantidad());
        existente.setDisponible(req.cantidad() - unidadesUsadas);
        existente.setFechaVencimiento(req.fechaVencimiento());
        existente.setPrecioCompra(req.precioCompra());
        existente.setPrecioVenta(req.precioVenta());
        existente.setTotal(calcularTotal(req.cantidad(), req.precioCompra()));
        existente.setInsumoLog(resolverInsumo(req.idInsumoLog()));
        existente.setMedicamentoLog(resolverMedicamento(req.idMedicamentoLog()));
        existente.setCompra(compra);

        LoteEntity actualizado = repository.save(existente);
        recalcularTotalCompra(compra);
        if (compraAnterior != null && !compraAnterior.getId().equals(compra.getId())) {
            recalcularTotalCompra(compraAnterior);
        }
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public LoteDTOs.Response obtenerPorId(Long id) {
        LoteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<LoteDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream().map(mapper::toDTO).toList();
        }
        return repository.findByEstado(activos).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<LoteDTOs.Response> buscarPorCodigo(String codigo, Boolean activos) {
        if (codigo == null || codigo.isBlank()) {
            return List.of();
        }
        String norm = codigo.trim();
        if (activos == null) {
            return repository.findByCodigoLoteContainingIgnoreCase(norm).stream().map(mapper::toDTO).toList();
        }
        return repository.findByCodigoLoteContainingIgnoreCaseAndEstado(norm, activos).stream().map(mapper::toDTO).toList();
    }

    /**
     * Inventario enriquecido: una fila por lote con datos del producto, la compra y el proveedor.
     * Los filtros son opcionales y se combinan entre sí.
     */
    @Transactional(readOnly = true)
    public List<LoteDTOs.InventarioResponse> obtenerInventario(
            String tipo, String nombre, Long proveedorId, Long viaAdminId,
            String estadoInventario, Boolean activos, Integer diasPorVencer) {
        boolean esMedicamento = resolverTipo(tipo);
        String estado = resolverEstadoInventario(estadoInventario);
        int dias = resolverDiasPorVencer(diasPorVencer);
        boolean filtrarActivos = "eliminados".equals(estado) ? false : (activos == null || activos);
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        String texto = nombre == null ? null : nombre.trim().toLowerCase(Locale.ROOT);

        Comparator<LoteDTOs.InventarioResponse> orden = Comparator
                .comparing(LoteDTOs.InventarioResponse::fechaVencimiento,
                        Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                .thenComparing(LoteDTOs.InventarioResponse::nombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        if ("sin-stock".equals(estado)) {
            return inventarioSinStock(esMedicamento, texto, proveedorId, viaAdminId, filtrarActivos)
                    .stream()
                    .sorted(orden)
                    .toList();
        }

        if ("stock-bajo".equals(estado)) {
            return inventarioStockBajo(esMedicamento, texto, proveedorId, viaAdminId, filtrarActivos)
                    .stream()
                    .sorted(orden)
                    .toList();
        }

        return repository.findByEstado(filtrarActivos).stream()
                .filter(lote -> esMedicamento
                        ? lote.getMedicamentoLog() != null
                        : lote.getInsumoLog() != null)
                .filter(lote -> coincideTexto(lote, esMedicamento, texto))
                .filter(lote -> coincideProveedor(lote, proveedorId))
                .filter(lote -> coincideViaAdmin(lote, viaAdminId, esMedicamento))
                .filter(lote -> coincideEstadoInventario(lote, estado, hoy, limite))
                .map(lote -> toInventarioResponse(lote, esMedicamento))
                .sorted(orden)
                .toList();
    }

    /**
     * Sin stock se resuelve por producto: se suman las unidades disponibles de todos sus lotes
     * y se incluyen los productos cuyo total es 0, aunque no tengan ningún lote registrado.
     */
    private List<LoteDTOs.InventarioResponse> inventarioSinStock(
            boolean esMedicamento, String texto, Long proveedorId, Long viaAdminId, boolean activos) {
        Map<Long, List<LoteEntity>> lotesPorItem = repository.findByEstado(activos).stream()
                .filter(lote -> esMedicamento
                        ? lote.getMedicamentoLog() != null
                        : lote.getInsumoLog() != null)
                .filter(lote -> coincideProveedor(lote, proveedorId))
                .collect(Collectors.groupingBy(lote -> esMedicamento
                        ? lote.getMedicamentoLog().getId()
                        : lote.getInsumoLog().getId()));

        if (esMedicamento) {
            return medicamentoLogRepository.findByEstado(activos).stream()
                    .filter(medicamento -> coincideTextoProducto(
                            medicamento.getNombre(), medicamento.getCodigo(), texto))
                    .filter(medicamento -> coincideViaAdmin(medicamento, viaAdminId))
                    .filter(medicamento -> lotesPorItem.containsKey(medicamento.getId())
                            || proveedorId == null)
                    .filter(medicamento -> disponibleTotal(
                            lotesPorItem.getOrDefault(medicamento.getId(), List.of())) == 0)
                    .map(medicamento -> {
                        LoteEntity representante = loteRepresentativo(
                                lotesPorItem.getOrDefault(medicamento.getId(), List.of()));
                        return representante == null
                                ? sinStockMedicamento(medicamento)
                                : toInventarioResponse(representante, true);
                    })
                    .toList();
        }

        return insumosLogRepository.findByEstado(activos).stream()
                .filter(insumo -> coincideTextoProducto(insumo.getNombre(), insumo.getCodigo(), texto))
                .filter(insumo -> lotesPorItem.containsKey(insumo.getId()) || proveedorId == null)
                .filter(insumo -> disponibleTotal(
                        lotesPorItem.getOrDefault(insumo.getId(), List.of())) == 0)
                .map(insumo -> {
                    LoteEntity representante = loteRepresentativo(
                            lotesPorItem.getOrDefault(insumo.getId(), List.of()));
                    return representante == null
                            ? sinStockInsumo(insumo)
                            : toInventarioResponse(representante, false);
                })
                .toList();
    }

    /**
     * Stock bajo por producto: se suman las unidades de todos sus lotes (sin importar el
     * proveedor). Solo se incluyen los productos con suma mayor a 0 y menor al umbral.
     */
    private List<LoteDTOs.InventarioResponse> inventarioStockBajo(
            boolean esMedicamento, String texto, Long proveedorId, Long viaAdminId, boolean activos) {
        LocalDate hoy = LocalDate.now();
        List<LoteEntity> lotes = repository.findByEstado(activos).stream()
                .filter(lote -> esMedicamento
                        ? lote.getMedicamentoLog() != null
                        : lote.getInsumoLog() != null)
                .filter(lote -> !estaVencido(lote, hoy))
                .filter(lote -> coincideTexto(lote, esMedicamento, texto))
                .filter(lote -> coincideViaAdmin(lote, viaAdminId, esMedicamento))
                .toList();

        Map<Long, Integer> totalesPorItem = lotes.stream().collect(Collectors.groupingBy(
                lote -> itemId(lote, esMedicamento),
                Collectors.summingInt(lote -> lote.getDisponible() == null ? 0 : lote.getDisponible())));

        return lotes.stream()
                .filter(lote -> coincideProveedor(lote, proveedorId))
                .filter(lote -> {
                    Integer total = totalesPorItem.get(itemId(lote, esMedicamento));
                    return total != null && total > 0 && total < STOCK_MINIMO;
                })
                .map(lote -> toInventarioResponse(lote, esMedicamento))
                .toList();
    }

    private Long itemId(LoteEntity lote, boolean esMedicamento) {
        return esMedicamento
                ? lote.getMedicamentoLog().getId()
                : lote.getInsumoLog().getId();
    }

    private boolean estaVencido(LoteEntity lote, LocalDate hoy) {
        return lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(hoy);
    }

    private boolean coincideViaAdmin(medicamentoLogEntity medicamento, Long viaAdminId) {
        return viaAdminId == null
                || (medicamento.getViaAdmin() != null
                        && viaAdminId.equals(medicamento.getViaAdmin().getId()));
    }

    private int disponibleTotal(List<LoteEntity> lotes) {
        return lotes.stream()
                .mapToInt(lote -> lote.getDisponible() == null ? 0 : lote.getDisponible())
                .sum();
    }

    private LoteEntity loteRepresentativo(List<LoteEntity> lotes) {
        return lotes.stream()
                .min(Comparator.comparing(LoteEntity::getFechaVencimiento,
                        Comparator.nullsLast(Comparator.<LocalDate>naturalOrder())))
                .orElse(null);
    }

    private LoteDTOs.InventarioResponse sinStockMedicamento(medicamentoLogEntity medicamento) {
        return new LoteDTOs.InventarioResponse(
                null,
                "MEDICAMENTO",
                medicamento.getId(),
                medicamento.getCodigo(),
                nombreCompletoMedicamento(medicamento),
                descripcionMedicamento(medicamento),
                medicamento.getViaAdmin() == null ? null : medicamento.getViaAdmin().getNombre(),
                medicamento.getPresentacion() == null ? null : medicamento.getPresentacion().getNombre(),
                0,
                0,
                null,
                null,
                null,
                null,
                medicamento.isEstado(),
                null,
                null);
    }

    private LoteDTOs.InventarioResponse sinStockInsumo(insumosLogEntity insumo) {
        return new LoteDTOs.InventarioResponse(
                null,
                "INSUMO",
                insumo.getId(),
                insumo.getCodigo(),
                nombreCompletoInsumo(insumo),
                descripcionInsumo(insumo),
                null,
                null,
                0,
                0,
                null,
                null,
                null,
                null,
                insumo.isEstado(),
                null,
                null);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        LoteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    /** Elimina un lote solo si aún no se ha entregado ninguna de sus unidades. */
    @Transactional
    public void eliminar(Long id) {
        LoteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con el ID: " + id));
        if (entity.getDisponible() < entity.getCantidad()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el lote " + entity.getCodigoLote()
                            + " porque ya se han entregado productos de este lote");
        }
        CompraEntity compra = entity.getCompra();
        repository.delete(entity);
        if (compra != null) {
            recalcularTotalCompra(compra);
        }
    }

    private boolean resolverTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return true;
        }
        return switch (tipo.trim().toLowerCase(Locale.ROOT)) {
            case "medicamento" -> true;
            case "insumo" -> false;
            default -> throw new IllegalArgumentException("El tipo debe ser 'medicamento' o 'insumo'");
        };
    }

    private String resolverEstadoInventario(String estadoInventario) {
        if (estadoInventario == null || estadoInventario.isBlank()) {
            return null;
        }
        String estado = estadoInventario.trim().toLowerCase(Locale.ROOT);
        if (!ESTADOS_INVENTARIO.contains(estado)) {
            throw new IllegalArgumentException(
                    "El estado de inventario debe ser uno de: " + ESTADOS_INVENTARIO);
        }
        return estado;
    }

    private int resolverDiasPorVencer(Integer diasPorVencer) {
        if (diasPorVencer == null) {
            return DIAS_POR_VENCER_DEFAULT;
        }
        if (diasPorVencer < 0) {
            throw new IllegalArgumentException("Los días por vencer no pueden ser negativos");
        }
        return diasPorVencer;
    }

    private boolean coincideTexto(LoteEntity lote, boolean esMedicamento, String texto) {
        String nombre = esMedicamento
                ? lote.getMedicamentoLog().getNombre()
                : lote.getInsumoLog().getNombre();
        String codigo = esMedicamento
                ? lote.getMedicamentoLog().getCodigo()
                : lote.getInsumoLog().getCodigo();
        return coincideTextoProducto(nombre, codigo, texto);
    }

    private boolean coincideTextoProducto(String nombre, String codigo, String texto) {
        if (texto == null || texto.isBlank()) {
            return true;
        }
        return contiene(nombre, texto) || contiene(codigo, texto);
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }

    private boolean coincideProveedor(LoteEntity lote, Long proveedorId) {
        if (proveedorId == null) {
            return true;
        }
        return lote.getCompra() != null
                && lote.getCompra().getProveedor() != null
                && proveedorId.equals(lote.getCompra().getProveedor().getId());
    }

    private boolean coincideViaAdmin(LoteEntity lote, Long viaAdminId, boolean esMedicamento) {
        if (viaAdminId == null || !esMedicamento) {
            return true;
        }
        return lote.getMedicamentoLog().getViaAdmin() != null
                && viaAdminId.equals(lote.getMedicamentoLog().getViaAdmin().getId());
    }

    private boolean coincideEstadoInventario(
            LoteEntity lote, String estado, LocalDate hoy, LocalDate limite) {
        if (estado == null) {
            return true;
        }
        Integer disponible = lote.getDisponible();
        LocalDate vencimiento = lote.getFechaVencimiento();
        return switch (estado) {
            case "vencidos" -> vencimiento != null
                    && vencimiento.isBefore(hoy)
                    && disponible != null && disponible > 0;
            case "por-vencer" -> vencimiento != null
                    && !vencimiento.isBefore(hoy)
                    && !vencimiento.isAfter(limite);
            default -> true;
        };
    }

    private LoteDTOs.InventarioResponse toInventarioResponse(LoteEntity lote, boolean esMedicamento) {
        if (esMedicamento) {
            medicamentoLogEntity medicamento = lote.getMedicamentoLog();
            return new LoteDTOs.InventarioResponse(
                    lote.getId(),
                    "MEDICAMENTO",
                    medicamento.getId(),
                    medicamento.getCodigo(),
                    nombreCompletoMedicamento(medicamento),
                    descripcionMedicamento(medicamento),
                    medicamento.getViaAdmin() == null ? null : medicamento.getViaAdmin().getNombre(),
                    medicamento.getPresentacion() == null ? null : medicamento.getPresentacion().getNombre(),
                    lote.getCantidad(),
                    lote.getDisponible(),
                    idProveedor(lote),
                    nombreProveedor(lote),
                    lote.getCodigoLote(),
                    lote.getFechaVencimiento(),
                    lote.isEstado(),
                    idCompra(lote),
                    codigoCompra(lote));
        }
        insumosLogEntity insumo = lote.getInsumoLog();
        return new LoteDTOs.InventarioResponse(
                lote.getId(),
                "INSUMO",
                insumo.getId(),
                insumo.getCodigo(),
                nombreCompletoInsumo(insumo),
                descripcionInsumo(insumo),
                null,
                null,
                lote.getCantidad(),
                lote.getDisponible(),
                idProveedor(lote),
                nombreProveedor(lote),
                lote.getCodigoLote(),
                lote.getFechaVencimiento(),
                lote.isEstado(),
                idCompra(lote),
                codigoCompra(lote));
    }

    /** Nombre visible del medicamento: nombre, marca y dosis con unidad (ej. "Amoxicilina Bayer 500mg"). */
    private String nombreCompletoMedicamento(medicamentoLogEntity medicamento) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, medicamento.getNombre());
        agregarParte(partes, medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre());
        String dosisUnidad = dosisConUnidadCompacta(medicamento);
        if (dosisUnidad != null) {
            partes.add(dosisUnidad);
        }
        return partes.isEmpty() ? null : String.join(" ", partes);
    }

    private String dosisConUnidadCompacta(medicamentoLogEntity medicamento) {
        if (medicamento.getDosis() == null) {
            return null;
        }
        String dosis = medicamento.getDosis().stripTrailingZeros().toPlainString();
        String unidad = medicamento.getUnidadMedida() == null
                ? null : medicamento.getUnidadMedida().getAbreviatura();
        return unidad == null || unidad.isBlank() ? dosis : dosis + unidad.trim();
    }

    private String descripcionMedicamento(medicamentoLogEntity medicamento) {
        List<String> partes = new ArrayList<>();
        String dosisUnidad = dosisConUnidad(medicamento);
        if (dosisUnidad != null) {
            partes.add(dosisUnidad);
        }
        agregarParte(partes, medicamento.getPresentacion() == null
                ? null : medicamento.getPresentacion().getNombre());
        agregarParte(partes, medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre());
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }

    /** Nombre visible del insumo: nombre, marca y detalle (ej. "Jeringa descartable Acme 5 ml"). */
    private String nombreCompletoInsumo(insumosLogEntity insumo) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, insumo.getNombre());
        agregarParte(partes, insumo.getMarca() == null ? null : insumo.getMarca().getNombre());
        agregarParte(partes, insumo.getDetalle());
        return partes.isEmpty() ? null : String.join(" ", partes);
    }

    private String descripcionInsumo(insumosLogEntity insumo) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, insumo.getMarca() == null ? null : insumo.getMarca().getNombre());
        agregarParte(partes, insumo.getDetalle());
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }

    private String dosisConUnidad(medicamentoLogEntity medicamento) {
        if (medicamento.getDosis() == null) {
            return null;
        }
        String dosis = medicamento.getDosis().stripTrailingZeros().toPlainString();
        String unidad = medicamento.getUnidadMedida() == null
                ? null : medicamento.getUnidadMedida().getAbreviatura();
        return unidad == null || unidad.isBlank() ? dosis : dosis + " " + unidad.trim();
    }

    private void agregarParte(List<String> partes, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(valor.trim());
        }
    }

    private Long idProveedor(LoteEntity lote) {
        return lote.getCompra() == null || lote.getCompra().getProveedor() == null
                ? null : lote.getCompra().getProveedor().getId();
    }

    private String nombreProveedor(LoteEntity lote) {
        return lote.getCompra() == null || lote.getCompra().getProveedor() == null
                ? null : lote.getCompra().getProveedor().getNombre();
    }

    private Long idCompra(LoteEntity lote) {
        return lote.getCompra() == null ? null : lote.getCompra().getId();
    }

    private String codigoCompra(LoteEntity lote) {
        return lote.getCompra() == null ? null : lote.getCompra().getCodigo();
    }

    private void validarTipoItem(LoteDTOs.Request req) {
        boolean conInsumo = req.idInsumoLog() != null;
        boolean conMedicamento = req.idMedicamentoLog() != null;
        if (conInsumo == conMedicamento) {
            throw new IllegalArgumentException("Debe indicar exactamente uno: idInsumoLog o idMedicamentoLog");
        }
    }

    private String normalizarCodigo(String codigoLote) {
        String codigo = StringNormalizer.normalizarTexto(codigoLote);
        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El código del lote es obligatorio");
        }
        return codigo;
    }

    private CompraEntity obtenerCompra(Long idCompra) {
        return compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada con el ID: " + idCompra));
    }

    private void validarNoDuplicadoEnCompra(Long idActual, String codigoLote, Long compraId) {
        repository.findByCodigoLoteIgnoreCaseAndCompra_Id(codigoLote, compraId)
                .filter(duplicado -> !duplicado.getId().equals(idActual))
                .ifPresent(duplicado -> {
                    throw new IllegalArgumentException("Ya existe un lote con el código " + codigoLote + " en esta compra");
                });
    }

    private void validarFechaVencimiento(LoteDTOs.Request req) {
        if (req.idMedicamentoLog() != null && req.fechaVencimiento() == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria para medicamentos");
        }
    }

    private void validarFechaConsistente(String codigoLote, LocalDate fechaVencimiento, Long idLoteExcluido) {
        if (fechaVencimiento == null) {
            // El lote no declara vencimiento (caso insumo): no hay fecha que comparar con otros lotes del mismo código.
            return;
        }
        for (LoteEntity lote : repository.findByCodigoLoteIgnoreCase(codigoLote)) {
            if (idLoteExcluido != null && lote.getId().equals(idLoteExcluido)) {
                continue;
            }
            if (!fechaVencimiento.equals(lote.getFechaVencimiento())) {
                throw new IllegalArgumentException(
                        "El lote " + codigoLote + " ya existe con fecha de vencimiento diferente. La fecha de vencimiento debe ser la misma para el mismo código de lote"
                );
            }
        }
    }

    private insumosLogEntity resolverInsumo(Long idInsumoLog) {
        if (idInsumoLog == null) {
            return null;
        }
        return insumosLogRepository.findById(idInsumoLog)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado con el ID: " + idInsumoLog));
    }

    private medicamentoLogEntity resolverMedicamento(Long idMedicamentoLog) {
        if (idMedicamentoLog == null) {
            return null;
        }
        return medicamentoLogRepository.findById(idMedicamentoLog)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado con el ID: " + idMedicamentoLog));
    }

    private BigDecimal calcularTotal(Integer cantidad, BigDecimal precioCompra) {
        return precioCompra.multiply(BigDecimal.valueOf(cantidad));
    }

    private void recalcularTotalCompra(CompraEntity compra) {
        BigDecimal suma = repository.sumTotalesPorCompra(compra.getId());
        compra.setTotal(suma != null ? suma : BigDecimal.ZERO);
        compraRepository.save(compra);
    }
}
