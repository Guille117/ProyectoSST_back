package com.example.demo.modules.farmacia.bajas;

import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BajaService {

    private final BajaRepository repository;
    private final LoteRepository loteRepository;
    private final motivoBajaRepository motivoBajaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public BajaDTOs.Response registrar(BajaDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        if (req.cantidad() == null || req.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        LoteEntity lote = loteRepository.findByIdForUpdate(req.idLote())
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado con el ID: " + req.idLote()));
        int stockAnterior = lote.getDisponible() == null ? 0 : lote.getDisponible();
        if (req.cantidad() > stockAnterior) {
            throw new IllegalArgumentException(
                    "No se puede dar de baja " + req.cantidad() + " unidades: el lote "
                            + lote.getCodigoLote() + " solo tiene " + stockAnterior + " disponibles");
        }

        motivoBajaEntity motivo = motivoBajaRepository.findById(req.idMotivoBaja())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Motivo de baja no encontrado con el ID: " + req.idMotivoBaja()));
        UsuarioEntity usuario = usuarioRepository.findById(req.idUsuario())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + req.idUsuario()));

        lote.setDisponible(stockAnterior - req.cantidad());
        loteRepository.save(lote);

        BajaEntity baja = BajaEntity.builder()
                .lote(lote)
                .cantidad(req.cantidad())
                .motivoBaja(motivo)
                .observacion(StringNormalizer.normalizarNullable(req.observacion()))
                .fechaHora(LocalDateTime.now())
                .usuario(usuario)
                .build();
        BajaEntity guardada = repository.save(baja);

        return toResponse(guardada, stockAnterior, lote.getDisponible());
    }

    @Transactional(readOnly = true)
    public List<BajaDTOs.ListadoResponse> listar(String tipo) {
        Boolean esMedicamento = resolverTipo(tipo);
        return repository.findAllByOrderByFechaHoraDesc().stream()
                .filter(baja -> esMedicamento == null
                        || esMedicamento == (baja.getLote().getMedicamentoLog() != null))
                .map(this::toListadoResponse)
                .toList();
    }

    /** null = ambos tipos; true = medicamentos; false = insumos. */
    private Boolean resolverTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }
        return switch (tipo.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "medicamento" -> true;
            case "insumo" -> false;
            default -> throw new IllegalArgumentException("El tipo debe ser 'medicamento' o 'insumo'");
        };
    }

    /** Revierte una baja: devuelve las unidades al lote y elimina el registro de la baja. */
    @Transactional
    public void revertir(Long id) {
        BajaEntity baja = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baja no encontrada con el ID: " + id));
        Long idLote = baja.getLote().getId();
        LoteEntity lote = loteRepository.findByIdForUpdate(idLote)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado con el ID: " + idLote));

        int disponible = lote.getDisponible() == null ? 0 : lote.getDisponible();
        lote.setDisponible(disponible + baja.getCantidad());
        loteRepository.save(lote);
        repository.delete(baja);
    }

    private BajaDTOs.ListadoResponse toListadoResponse(BajaEntity baja) {
        LoteEntity lote = baja.getLote();
        return new BajaDTOs.ListadoResponse(
                baja.getId(),
                nombreProducto(lote),
                lote.getCodigoLote(),
                baja.getMotivoBaja().getNombre(),
                baja.getObservacion(),
                baja.getFechaHora(),
                nombreUsuario(baja.getUsuario()));
    }

    /** Primer nombre y los dos apellidos; si no hay persona registrada usa el username. */
    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario.getPersona() == null) {
            return usuario.getUsername();
        }
        List<String> partes = new ArrayList<>();
        agregarParte(partes, usuario.getPersona().getPrimerNombre());
        agregarParte(partes, usuario.getPersona().getPrimerApellido());
        agregarParte(partes, usuario.getPersona().getSegundoApellido());
        return partes.isEmpty() ? usuario.getUsername() : String.join(" ", partes);
    }

    private String nombreProducto(LoteEntity lote) {
        if (lote.getMedicamentoLog() != null) {
            return nombreMedicamento(lote.getMedicamentoLog());
        }
        return nombreInsumo(lote.getInsumoLog());
    }

    /** Nombre visible: nombre, marca, presentación y dosis con unidad (ej. "Penicilina Bayer Tabletas 500mg"). */
    private String nombreMedicamento(medicamentoLogEntity medicamento) {
        List<String> partes = new ArrayList<>();
        agregarParte(partes, medicamento.getNombre());
        agregarParte(partes, medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre());
        agregarParte(partes, medicamento.getPresentacion() == null
                ? null : medicamento.getPresentacion().getNombre());
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
        String unidad = medicamento.getUnidadMedida() == null
                ? null : medicamento.getUnidadMedida().getAbreviatura();
        return unidad == null || unidad.isBlank() ? dosis : dosis + unidad.trim();
    }

    private String nombreInsumo(insumosLogEntity insumo) {
        if (insumo == null) {
            return null;
        }
        List<String> partes = new ArrayList<>();
        agregarParte(partes, insumo.getNombre());
        agregarParte(partes, insumo.getMarca() == null ? null : insumo.getMarca().getNombre());
        agregarParte(partes, insumo.getDetalle());
        return partes.isEmpty() ? null : String.join(" ", partes);
    }

    private void agregarParte(List<String> partes, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(valor.trim());
        }
    }

    private BajaDTOs.Response toResponse(BajaEntity baja, int stockAnterior, int stockActual) {
        LoteEntity lote = baja.getLote();
        return new BajaDTOs.Response(
                baja.getId(),
                lote.getId(),
                lote.getCodigoLote(),
                baja.getCantidad(),
                stockAnterior,
                stockActual,
                baja.getMotivoBaja().getId(),
                baja.getMotivoBaja().getNombre(),
                baja.getObservacion(),
                baja.getFechaHora(),
                baja.getUsuario().getId(),
                baja.getUsuario().getUsername());
    }
}
