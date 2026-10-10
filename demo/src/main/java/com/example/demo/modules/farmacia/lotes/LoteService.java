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
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoteService {

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

        existente.setCodigoLote(codigoLote);
        existente.setCantidad(req.cantidad());
        existente.setDisponible(req.cantidad());
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

    @Transactional
    public void cambiarEstado(Long id) {
        LoteEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
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
