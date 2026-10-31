package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimientoMedico.EstadoProcedimientoMedico;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoEntity;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRecursoEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudSalidaService {

    private final SolicitudSalidaRepository repository;
    private final SolicitudSalidaMovimientoRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final motivoBajaRepository motivoBajaRepository;
    private final EpisodioRepository episodioRepository;
    private final medicamentoLogRepository medicamentoLogRepository;
    private final insumosLogRepository insumosLogRepository;

    // ------------------- SINCRONIZACIÓN CON EL PROCEDIMIENTO -------------------

    @Transactional
    public void sincronizar(procedimientoMedicoEntity procedimiento) {
        if (procedimiento == null || procedimiento.getId() == null) {
            throw new IllegalArgumentException("El procedimiento médico es obligatorio");
        }

        SolicitudSalidaEntity solicitud = repository
                .findByProcedimientoMedico_IdForUpdate(procedimiento.getId())
                .orElse(null);

        // Un procedimiento eliminado no debe reactivar su solicitud de farmacia.
        if (procedimiento.getEstado() == EstadoProcedimientoMedico.ELIMINADO) {
            if (solicitud == null) {
                return;
            }
            validarSinEntregas(solicitud, "No se puede reactivar la solicitud de un procedimiento eliminado");
            for (SolicitudSalidaDetalleEntity detalle : solicitud.getDetalles()) {
                detalle.setEstado(EstadoDetalleSalida.CANCELADA);
            }
            solicitud.setEstado(EstadoSolicitudSalida.CANCELADA);
            repository.save(solicitud);
            return;
        }

        Map<String, RecursoAgrupado> recursos = agruparRecursos(procedimiento.getRecursos());

        if (recursos.isEmpty()) {
            if (solicitud == null) {
                return;
            }
            validarSinEntregas(solicitud, "No se pueden quitar los recursos del procedimiento");
            depurarDetalles(solicitud);
            recalcularEstadoSolicitud(solicitud);
            repository.save(solicitud);
            return;
        }

        if (solicitud == null) {
            solicitud = SolicitudSalidaEntity.builder()
                    .codigo(String.format("SOL-%05d", procedimiento.getId()))
                    .procedimientoMedico(procedimiento)
                    .episodio(procedimiento.getEpisodio())
                    .usuarioSolicita(procedimiento.getUsuario())
                    .fechaSolicitud(LocalDateTime.now())
                    .estado(EstadoSolicitudSalida.PENDIENTE)
                    .detalles(new ArrayList<>())
                    .build();
        } else {
            solicitud.setProcedimientoMedico(procedimiento);
            solicitud.setEpisodio(procedimiento.getEpisodio());
            solicitud.setUsuarioSolicita(procedimiento.getUsuario());
        }

        sincronizarDetalles(solicitud, recursos);
    }

    /**
     * Crea o actualiza la solicitud de Farmacia generada por el alta médica. El código
     * SOL-A&lt;altaId&gt; identifica la solicitud sin depender de un procedimiento.
     */
    @Transactional
    public void sincronizarDesdeAlta(Long altaId, EpisodioEntity episodio, UsuarioEntity usuario,
            List<SolicitudSalidaDTOs.LineaAlta> lineas) {
        if (altaId == null) {
            throw new IllegalArgumentException("El alta es obligatoria");
        }
        if (episodio == null || usuario == null) {
            throw new IllegalArgumentException("El episodio y el usuario del alta son obligatorios");
        }

        String codigo = codigoAlta(altaId);
        SolicitudSalidaEntity solicitud = repository.findByCodigoForUpdate(codigo).orElse(null);
        Map<String, RecursoAgrupado> recursos = agruparLineasAlta(lineas);

        if (recursos.isEmpty()) {
            if (solicitud == null) {
                return;
            }
            validarSinEntregas(solicitud, "No se pueden quitar los medicamentos del alta");
            depurarDetalles(solicitud);
            recalcularEstadoSolicitud(solicitud);
            repository.save(solicitud);
            return;
        }

        if (solicitud == null) {
            solicitud = SolicitudSalidaEntity.builder()
                    .codigo(codigo)
                    .procedimientoMedico(null)
                    .episodio(episodio)
                    .usuarioSolicita(usuario)
                    .fechaSolicitud(LocalDateTime.now())
                    .estado(EstadoSolicitudSalida.PENDIENTE)
                    .detalles(new ArrayList<>())
                    .build();
        } else {
            solicitud.setEpisodio(episodio);
            solicitud.setUsuarioSolicita(usuario);
        }

        sincronizarDetalles(solicitud, recursos);
    }

    /** Cancela la solicitud generada por un alta si aún no tiene entregas. */
    @Transactional
    public void cancelarDesdeAlta(Long altaId) {
        if (altaId == null) {
            return;
        }
        SolicitudSalidaEntity solicitud = repository.findByCodigoForUpdate(codigoAlta(altaId))
                .orElse(null);
        if (solicitud == null) {
            return;
        }
        cancelarSolicitud(solicitud, "No se puede anular el alta");
    }

    private String codigoAlta(Long altaId) {
        return String.format("SOL-A%05d", altaId);
    }

    private Map<String, RecursoAgrupado> agruparLineasAlta(
            List<SolicitudSalidaDTOs.LineaAlta> lineas) {
        Map<String, RecursoAgrupado> agrupados = new LinkedHashMap<>();
        if (lineas == null) {
            return agrupados;
        }

        for (SolicitudSalidaDTOs.LineaAlta linea : lineas) {
            if (linea == null || linea.referenciaId() == null || linea.cantidad() == null
                    || linea.cantidad() <= 0) {
                continue;
            }

            String clave = clave(TipoRecursoProcedimiento.MEDICAMENTO, linea.referenciaId());
            RecursoAgrupado previo = agrupados.get(clave);
            agrupados.put(clave, previo == null
                    ? new RecursoAgrupado(
                            TipoRecursoProcedimiento.MEDICAMENTO, linea.referenciaId(),
                            linea.nombre(), linea.cantidad())
                    : new RecursoAgrupado(
                            previo.tipo(), previo.referenciaId(), previo.nombre(),
                            Math.addExact(previo.cantidad(), linea.cantidad())));
        }

        return agrupados;
    }

    private void sincronizarDetalles(SolicitudSalidaEntity solicitud,
            Map<String, RecursoAgrupado> recursos) {
        Map<String, SolicitudSalidaDetalleEntity> existentes = solicitud.getDetalles().stream()
                .collect(Collectors.toMap(
                        detalle -> clave(detalle.getTipo(), detalle.getReferenciaId()),
                        detalle -> detalle,
                        (primero, segundo) -> primero,
                        LinkedHashMap::new));

        for (Map.Entry<String, RecursoAgrupado> entrada : recursos.entrySet()) {
            RecursoAgrupado recurso = entrada.getValue();
            SolicitudSalidaDetalleEntity detalle = existentes.get(entrada.getKey());

            if (detalle == null) {
                detalle = SolicitudSalidaDetalleEntity.builder()
                        .tipo(recurso.tipo())
                        .referenciaId(recurso.referenciaId())
                        .nombre(recurso.nombre())
                        .cantidadSolicitada(recurso.cantidad())
                        .cantidadEntregada(0)
                        .estado(EstadoDetalleSalida.PENDIENTE)
                        .build();
                solicitud.agregarDetalle(detalle);
                continue;
            }

            int entregada = valor(detalle.getCantidadEntregada());
            if (recurso.cantidad() < entregada) {
                throw new IllegalArgumentException(
                        "No se puede reducir la cantidad solicitada de " + detalle.getNombre()
                                + " a " + recurso.cantidad() + " porque ya se entregaron "
                                + entregada + " unidades. Registre primero la devolución en Farmacia.");
            }

            detalle.setTipo(recurso.tipo());
            detalle.setReferenciaId(recurso.referenciaId());
            detalle.setNombre(recurso.nombre());
            detalle.setCantidadSolicitada(recurso.cantidad());
            detalle.setEstado(calcularEstadoDetalle(detalle));
        }

        for (SolicitudSalidaDetalleEntity detalle : new ArrayList<>(solicitud.getDetalles())) {
            if (recursos.containsKey(clave(detalle.getTipo(), detalle.getReferenciaId()))) {
                continue;
            }

            int entregada = valor(detalle.getCantidadEntregada());
            if (entregada > 0) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el recurso " + detalle.getNombre()
                                + " de la solicitud porque ya se entregaron " + entregada
                                + " unidades. Registre primero la devolución en Farmacia.");
            }

            if (tieneMovimientos(detalle)) {
                detalle.setEstado(EstadoDetalleSalida.CANCELADA);
            } else {
                solicitud.getDetalles().remove(detalle);
            }
        }

        recalcularEstadoSolicitud(solicitud);
        repository.save(solicitud);
    }

    private void cancelarSolicitud(SolicitudSalidaEntity solicitud, String mensaje) {
        validarSinEntregas(solicitud, mensaje);
        for (SolicitudSalidaDetalleEntity detalle : solicitud.getDetalles()) {
            detalle.setEstado(EstadoDetalleSalida.CANCELADA);
        }
        solicitud.setEstado(EstadoSolicitudSalida.CANCELADA);
        repository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> obtenerCantidadesEntregadas(Long procedimientoId) {
        if (procedimientoId == null) {
            return Map.of();
        }

        return repository.findByProcedimientoMedico_Id(procedimientoId)
                .map(solicitud -> solicitud.getDetalles().stream()
                        .filter(detalle -> detalle.getEstado() != EstadoDetalleSalida.CANCELADA)
                        .filter(detalle -> valor(detalle.getCantidadEntregada()) > 0)
                        .collect(Collectors.toMap(
                                detalle -> clave(detalle.getTipo(), detalle.getReferenciaId()),
                                detalle -> valor(detalle.getCantidadEntregada()),
                                Integer::sum,
                                LinkedHashMap::new)))
                .orElseGet(LinkedHashMap::new);
    }

    @Transactional
    public void cancelarPorProcedimiento(Long procedimientoId) {
        if (procedimientoId == null) {
            return;
        }

        SolicitudSalidaEntity solicitud = repository
                .findByProcedimientoMedico_IdForUpdate(procedimientoId)
                .orElse(null);
        if (solicitud == null) {
            return;
        }

        cancelarSolicitud(solicitud, "No se puede eliminar el procedimiento");
    }

    // ------------------- CONSULTAS -------------------

    @Transactional(readOnly = true)
    public List<SolicitudSalidaDTOs.ResumenResponse> listar(
            EstadoSolicitudSalida estado, String texto) {
        List<SolicitudSalidaEntity> base = estado == null
                ? repository.findAllByOrderByFechaSolicitudDesc()
                : repository.findByEstadoOrderByFechaSolicitudDesc(estado);

        String filtro = normalizarFiltro(texto);
        Map<Long, SolicitudSalidaEntity> unicas = new LinkedHashMap<>();
        for (SolicitudSalidaEntity solicitud : base) {
            unicas.putIfAbsent(solicitud.getId(), solicitud);
        }

        return unicas.values().stream()
                .filter(solicitud -> estado != null
                        || solicitud.getEstado() != EstadoSolicitudSalida.CANCELADA)
                .filter(solicitud -> coincide(solicitud, filtro))
                .map(this::toResumen)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudSalidaDTOs.Response obtener(Long id) {
        SolicitudSalidaEntity solicitud = repository.findByIdConDetalles(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud de salida no encontrada con el ID: " + id));
        return toResponse(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudSalidaDTOs.HistorialResponse> historial(Long id) {
        if (id == null || !repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Solicitud de salida no encontrada con el ID: " + id);
        }

        return movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(id).stream()
                .map(this::toHistorial)
                .toList();
    }

    // ------------------- SALIDAS DIRECTAS -------------------

    /**
     * Registra una salida directa de Farmacia ligada al episodio. No crea una
     * solicitud pendiente: reutiliza la entrega para descontar el lote elegido
     * y deja la salida completada en la misma transacción.
     */
    @Transactional
    public SolicitudSalidaDTOs.Response crearDirecta(
            SolicitudSalidaDTOs.SalidaDirectaRequest request) {
        if (request == null || request.episodioId() == null || request.usuarioId() == null) {
            throw new IllegalArgumentException("El episodio y el usuario son obligatorios");
        }
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto para la salida");
        }

        EpisodioEntity episodio = episodioRepository.findById(request.episodioId())
                .filter(EpisodioEntity::isEstado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El episodio no existe o está cerrado."));
        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + request.usuarioId()));

        Map<String, LineaDirectaAgrupada> agrupadas = new LinkedHashMap<>();
        for (SolicitudSalidaDTOs.SalidaDirectaLineaRequest linea : request.lineas()) {
            if (linea == null || linea.tipo() == null || linea.referenciaId() == null
                    || linea.cantidad() == null || linea.cantidad() <= 0
                    || linea.loteId() == null) {
                throw new IllegalArgumentException("Los datos de la salida son obligatorios");
            }

            String clave = clave(linea.tipo(), linea.referenciaId());
            LineaDirectaAgrupada previa = agrupadas.get(clave);
            if (previa == null) {
                agrupadas.put(clave, new LineaDirectaAgrupada(
                        linea.tipo(), linea.referenciaId(), linea.cantidad()));
            } else {
                agrupadas.put(clave, new LineaDirectaAgrupada(
                        previa.tipo(),
                        previa.referenciaId(),
                        Math.addExact(previa.cantidad(), linea.cantidad())));
            }
        }

        SolicitudSalidaEntity solicitud = SolicitudSalidaEntity.builder()
                .codigo("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
                .procedimientoMedico(null)
                .episodio(episodio)
                .usuarioSolicita(usuario)
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoSolicitudSalida.PENDIENTE)
                .detalles(new ArrayList<>())
                .build();

        Map<String, SolicitudSalidaDetalleEntity> detalles = new LinkedHashMap<>();
        for (LineaDirectaAgrupada linea : agrupadas.values()) {
            String clave = clave(linea.tipo(), linea.referenciaId());
            SolicitudSalidaDetalleEntity detalle = SolicitudSalidaDetalleEntity.builder()
                    .tipo(linea.tipo())
                    .referenciaId(linea.referenciaId())
                    .nombre(nombreProducto(linea.tipo(), linea.referenciaId()))
                    .cantidadSolicitada(linea.cantidad())
                    .cantidadEntregada(0)
                    .estado(EstadoDetalleSalida.PENDIENTE)
                    .build();
            solicitud.agregarDetalle(detalle);
            detalles.put(clave, detalle);
        }

        SolicitudSalidaEntity guardada = repository.saveAndFlush(solicitud);
        guardada.setCodigo(String.format("SAL-M%05d", guardada.getId()));
        repository.save(guardada);

        List<SolicitudSalidaDTOs.EntregaLineaRequest> lineasEntrega = new ArrayList<>();
        for (SolicitudSalidaDTOs.SalidaDirectaLineaRequest linea : request.lineas()) {
            SolicitudSalidaDetalleEntity detalle =
                    detalles.get(clave(linea.tipo(), linea.referenciaId()));
            lineasEntrega.add(new SolicitudSalidaDTOs.EntregaLineaRequest(
                    detalle.getId(), linea.loteId(), linea.cantidad()));
        }

        return entregar(guardada.getId(), new SolicitudSalidaDTOs.EntregaRequest(
                usuario.getId(), lineasEntrega));
    }

    private String nombreProducto(TipoRecursoProcedimiento tipo, Long referenciaId) {
        if (tipo == TipoRecursoProcedimiento.MEDICAMENTO) {
            medicamentoLogEntity medicamento = medicamentoLogRepository
                    .findByIdAndEstado(referenciaId, true)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Medicamento no encontrado con el ID: " + referenciaId));
            return nombreMedicamento(medicamento);
        }

        insumosLogEntity insumo = insumosLogRepository.findById(referenciaId)
                .filter(insumosLogEntity::isEstado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Insumo no encontrado con el ID: " + referenciaId));
        return insumo.getNombre();
    }

    private String nombreMedicamento(medicamentoLogEntity medicamento) {
        List<String> partes = new ArrayList<>();
        if (medicamento.getNombre() != null && !medicamento.getNombre().isBlank()) {
            partes.add(medicamento.getNombre().trim());
        }
        if (medicamento.getMarca() != null && medicamento.getMarca().getNombre() != null
                && !medicamento.getMarca().getNombre().isBlank()) {
            partes.add(medicamento.getMarca().getNombre().trim());
        }
        if (medicamento.getPresentacion() != null && medicamento.getPresentacion().getNombre() != null
                && !medicamento.getPresentacion().getNombre().isBlank()) {
            partes.add(medicamento.getPresentacion().getNombre().trim());
        }

        String nombre = String.join(" ", partes);
        if (medicamento.getDosis() == null) {
            return nombre;
        }

        String dosis = medicamento.getDosis().stripTrailingZeros().toPlainString();
        String unidad = medicamento.getUnidadMedida() == null
                || medicamento.getUnidadMedida().getAbreviatura() == null
                ? ""
                : medicamento.getUnidadMedida().getAbreviatura().trim();
        return nombre + " de " + dosis + (unidad.isBlank() ? "" : " " + unidad);
    }

    private SolicitudSalidaDTOs.HistorialResponse toHistorial(
            SolicitudSalidaMovimientoEntity movimiento) {
        SolicitudSalidaDetalleEntity detalle = movimiento.getDetalle();
        LoteEntity lote = movimiento.getLote();

        return new SolicitudSalidaDTOs.HistorialResponse(
                movimiento.getId(),
                movimiento.getFechaHora(),
                movimiento.getTipoMovimiento(),
                detalle == null ? null : detalle.getTipo(),
                detalle == null ? "" : detalle.getNombre(),
                lote == null ? "" : lote.getCodigoLote(),
                movimiento.getCantidad(),
                movimiento.getCondicion(),
                movimiento.getMotivoBaja() == null ? null : movimiento.getMotivoBaja().getNombre(),
                nombreUsuario(movimiento.getUsuario()),
                movimiento.getObservacion());
    }

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return "";
        }

        String nombre = usuario.getPersona() == null
                ? null
                : usuario.getPersona().getNombreCompleto();
        return nombre == null || nombre.isBlank() ? usuario.getUsername() : nombre;
    }

    // ------------------- DESPACHO -------------------

    @Transactional
    public SolicitudSalidaDTOs.Response entregar(
            Long id, SolicitudSalidaDTOs.EntregaRequest request) {
        validarEntrega(request);

        SolicitudSalidaEntity solicitud = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud de salida no encontrada con el ID: " + id));
        validarSolicitudActiva(solicitud);

        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + request.usuarioId()));

        Map<Long, SolicitudSalidaDetalleEntity> detalles = solicitud.getDetalles().stream()
                .filter(detalle -> detalle.getId() != null)
                .collect(Collectors.toMap(
                        SolicitudSalidaDetalleEntity::getId,
                        detalle -> detalle,
                        (primero, segundo) -> primero));

        Set<String> procesados = new HashSet<>();
        for (SolicitudSalidaDTOs.EntregaLineaRequest linea : request.lineas()) {
            if (linea == null || linea.detalleId() == null || linea.loteId() == null
                    || linea.cantidad() == null || linea.cantidad() <= 0) {
                throw new IllegalArgumentException("Los datos de la entrega son obligatorios");
            }
            if (!procesados.add(linea.detalleId() + "#" + linea.loteId())) {
                throw new IllegalArgumentException(
                        "No se puede repetir el mismo detalle y lote en una entrega");
            }

            SolicitudSalidaDetalleEntity detalle = detalles.get(linea.detalleId());
            if (detalle == null || detalle.getEstado() == EstadoDetalleSalida.CANCELADA) {
                throw new IllegalArgumentException(
                        "Detalle de salida no encontrado con el ID: " + linea.detalleId());
            }

            int pendiente = detalle.pendiente();
            if (linea.cantidad() > pendiente) {
                throw new IllegalArgumentException(
                        "La cantidad a entregar de " + detalle.getNombre()
                                + " supera el pendiente (" + pendiente + ")");
            }

            LoteEntity lote = loteRepository.findByIdForUpdate(linea.loteId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Lote no encontrado con el ID: " + linea.loteId()));
            validarLoteParaEntrega(lote, detalle, linea.cantidad());

            lote.setDisponible(valor(lote.getDisponible()) - linea.cantidad());
            loteRepository.save(lote);

            detalle.setCantidadEntregada(valor(detalle.getCantidadEntregada()) + linea.cantidad());
            detalle.setEstado(calcularEstadoDetalle(detalle));

            movimientoRepository.save(SolicitudSalidaMovimientoEntity.builder()
                    .detalle(detalle)
                    .lote(lote)
                    .tipoMovimiento(TipoMovimientoSalida.ENTREGA)
                    .cantidad(linea.cantidad())
                    .usuario(usuario)
                    .fechaHora(LocalDateTime.now())
                    .build());
        }

        recalcularEstadoSolicitud(solicitud);
        repository.save(solicitud);
        movimientoRepository.flush();
        return toResponse(solicitud);
    }

    // ------------------- DEVOLUCIONES -------------------

    @Transactional
    public SolicitudSalidaDTOs.Response devolver(
            Long id, SolicitudSalidaDTOs.DevolucionRequest request) {
        validarDevolucion(request);

        SolicitudSalidaEntity solicitud = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud de salida no encontrada con el ID: " + id));
        validarSolicitudActiva(solicitud);

        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + request.usuarioId()));

        Map<Long, SolicitudSalidaDetalleEntity> detalles = solicitud.getDetalles().stream()
                .filter(detalle -> detalle.getId() != null)
                .collect(Collectors.toMap(
                        SolicitudSalidaDetalleEntity::getId,
                        detalle -> detalle,
                        (primero, segundo) -> primero));

        Set<String> procesados = new HashSet<>();
        for (SolicitudSalidaDTOs.DevolucionLineaRequest linea : request.lineas()) {
            if (linea == null || linea.detalleId() == null || linea.loteId() == null
                    || linea.cantidad() == null || linea.cantidad() <= 0
                    || linea.condicion() == null || linea.motivoBajaId() == null) {
                throw new IllegalArgumentException("Los datos de la devolución son obligatorios");
            }
            if (!procesados.add(linea.detalleId() + "#" + linea.loteId())) {
                throw new IllegalArgumentException(
                        "No se puede repetir el mismo detalle y lote en una devolución");
            }

            SolicitudSalidaDetalleEntity detalle = detalles.get(linea.detalleId());
            if (detalle == null || detalle.getEstado() == EstadoDetalleSalida.CANCELADA) {
                throw new IllegalArgumentException(
                        "Detalle de salida no encontrado con el ID: " + linea.detalleId());
            }

            LoteEntity lote = loteRepository.findById(linea.loteId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Lote no encontrado con el ID: " + linea.loteId()));
            validarLotePerteneceAlDetalle(lote, detalle);

            motivoBajaEntity motivo = motivoBajaRepository.findById(linea.motivoBajaId())
                    .filter(motivoBajaEntity::isEstado)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Motivo de devolución no encontrado con el ID: " + linea.motivoBajaId()));

            int disponibleDevolver = netoEntregado(detalle, lote.getId());
            if (linea.cantidad() > disponibleDevolver) {
                throw new IllegalArgumentException(
                        "La cantidad a devolver de " + detalle.getNombre()
                                + " supera lo entregado en el lote ("
                                + disponibleDevolver + ")");
            }

            if (linea.condicion() == CondicionDevolucion.APTO) {
                lote = loteRepository.findByIdForUpdate(lote.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Lote no encontrado con el ID: " + linea.loteId()));
                lote.setDisponible(valor(lote.getDisponible()) + linea.cantidad());
                loteRepository.save(lote);
            }

            movimientoRepository.save(SolicitudSalidaMovimientoEntity.builder()
                    .detalle(detalle)
                    .lote(lote)
                    .tipoMovimiento(TipoMovimientoSalida.DEVOLUCION)
                    .cantidad(linea.cantidad())
                    .condicion(linea.condicion())
                    .motivoBaja(motivo)
                    .observacion(StringNormalizer.normalizarNullable(linea.observacion()))
                    .usuario(usuario)
                    .fechaHora(LocalDateTime.now())
                    .build());

            detalle.setCantidadEntregada(
                    Math.max(0, valor(detalle.getCantidadEntregada()) - linea.cantidad()));
            detalle.setEstado(calcularEstadoDetalle(detalle));
        }

        recalcularEstadoSolicitud(solicitud);
        repository.save(solicitud);
        movimientoRepository.flush();
        return toResponse(solicitud);
    }

    // ------------------- APOYO -------------------

    private Map<String, RecursoAgrupado> agruparRecursos(
            List<procedimientoMedicoRecursoEntity> recursos) {
        Map<String, RecursoAgrupado> agrupados = new LinkedHashMap<>();
        if (recursos == null) {
            return agrupados;
        }

        for (procedimientoMedicoRecursoEntity recurso : recursos) {
            if (recurso == null || recurso.getTipo() == null || recurso.getReferenciaId() == null) {
                throw new IllegalArgumentException("Los datos del recurso son obligatorios");
            }
            if (recurso.getCantidad() == null || recurso.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad del recurso debe ser mayor a 0");
            }

            String clave = clave(recurso.getTipo(), recurso.getReferenciaId());
            RecursoAgrupado previo = agrupados.get(clave);
            if (previo == null) {
                agrupados.put(clave, new RecursoAgrupado(
                        recurso.getTipo(),
                        recurso.getReferenciaId(),
                        recurso.getNombre(),
                        recurso.getCantidad()));
            } else {
                agrupados.put(clave, new RecursoAgrupado(
                        previo.tipo(),
                        previo.referenciaId(),
                        previo.nombre(),
                        Math.addExact(previo.cantidad(), recurso.getCantidad())));
            }
        }

        return agrupados;
    }

    private void validarSinEntregas(SolicitudSalidaEntity solicitud, String mensaje) {
        boolean conEntregas = solicitud.getDetalles().stream()
                .anyMatch(detalle -> valor(detalle.getCantidadEntregada()) > 0);
        if (conEntregas) {
            throw new IllegalArgumentException(
                    mensaje + " porque su solicitud de Farmacia ya tiene entregas. "
                            + "Registre primero las devoluciones.");
        }
    }

    private void depurarDetalles(SolicitudSalidaEntity solicitud) {
        for (SolicitudSalidaDetalleEntity detalle : new ArrayList<>(solicitud.getDetalles())) {
            if (tieneMovimientos(detalle)) {
                detalle.setEstado(EstadoDetalleSalida.CANCELADA);
            } else {
                solicitud.getDetalles().remove(detalle);
            }
        }
    }

    private boolean tieneMovimientos(SolicitudSalidaDetalleEntity detalle) {
        return detalle.getId() != null && movimientoRepository.existsByDetalle_Id(detalle.getId());
    }

    private EstadoDetalleSalida calcularEstadoDetalle(SolicitudSalidaDetalleEntity detalle) {
        if (detalle.getEstado() == EstadoDetalleSalida.CANCELADA) {
            return EstadoDetalleSalida.CANCELADA;
        }

        int solicitada = valor(detalle.getCantidadSolicitada());
        int entregada = valor(detalle.getCantidadEntregada());

        if (entregada <= 0) {
            return EstadoDetalleSalida.PENDIENTE;
        }
        if (entregada >= solicitada) {
            return EstadoDetalleSalida.COMPLETADA;
        }
        return EstadoDetalleSalida.PARCIAL;
    }

    private void recalcularEstadoSolicitud(SolicitudSalidaEntity solicitud) {
        List<SolicitudSalidaDetalleEntity> activos = solicitud.getDetalles().stream()
                .filter(detalle -> detalle.getEstado() != EstadoDetalleSalida.CANCELADA)
                .toList();

        if (activos.isEmpty()) {
            solicitud.setEstado(EstadoSolicitudSalida.CANCELADA);
            return;
        }

        if (activos.stream().allMatch(detalle -> detalle.getEstado() == EstadoDetalleSalida.COMPLETADA)) {
            solicitud.setEstado(EstadoSolicitudSalida.COMPLETADA);
            return;
        }

        boolean algunaEntrega = activos.stream()
                .anyMatch(detalle -> valor(detalle.getCantidadEntregada()) > 0);
        solicitud.setEstado(algunaEntrega
                ? EstadoSolicitudSalida.PARCIAL
                : EstadoSolicitudSalida.PENDIENTE);
    }

    private void validarSolicitudActiva(SolicitudSalidaEntity solicitud) {
        if (solicitud.getEstado() == EstadoSolicitudSalida.CANCELADA) {
            throw new IllegalArgumentException(
                    "La solicitud de salida está cancelada y no permite movimientos");
        }
    }

    private void validarLoteParaEntrega(
            LoteEntity lote, SolicitudSalidaDetalleEntity detalle, int cantidad) {
        validarLotePerteneceAlDetalle(lote, detalle);

        if (!lote.isEstado()) {
            throw new IllegalArgumentException(
                    "El lote " + lote.getCodigoLote() + " está inactivo");
        }
        if (lote.getFechaVencimiento() != null
                && lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "El lote " + lote.getCodigoLote() + " está vencido");
        }
        if (cantidad > valor(lote.getDisponible())) {
            throw new IllegalArgumentException(
                    "El lote " + lote.getCodigoLote() + " solo tiene "
                            + valor(lote.getDisponible()) + " disponibles");
        }
    }

    private void validarLotePerteneceAlDetalle(
            LoteEntity lote, SolicitudSalidaDetalleEntity detalle) {
        if (detalle.getTipo() == TipoRecursoProcedimiento.MEDICAMENTO) {
            if (lote.getMedicamentoLog() == null
                    || !detalle.getReferenciaId().equals(lote.getMedicamentoLog().getId())) {
                throw new IllegalArgumentException(
                        "El lote no corresponde al medicamento de la solicitud");
            }
            return;
        }

        if (lote.getInsumoLog() == null
                || !detalle.getReferenciaId().equals(lote.getInsumoLog().getId())) {
            throw new IllegalArgumentException(
                    "El lote no corresponde al insumo de la solicitud");
        }
    }

    private int netoEntregado(SolicitudSalidaDetalleEntity detalle, Long loteId) {
        if (detalle.getId() == null) {
            return 0;
        }

        int entregado = 0;
        int devuelto = 0;
        for (SolicitudSalidaMovimientoEntity movimiento
                : movimientoRepository.findByDetalle_IdOrderByFechaHoraAsc(detalle.getId())) {
            if (movimiento.getLote() == null || !loteId.equals(movimiento.getLote().getId())) {
                continue;
            }
            if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.ENTREGA) {
                entregado += valor(movimiento.getCantidad());
            } else if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.DEVOLUCION) {
                devuelto += valor(movimiento.getCantidad());
            }
        }
        return Math.max(0, entregado - devuelto);
    }

    private List<SolicitudSalidaDTOs.LoteDisponibleResponse> lotesDisponibles(
            SolicitudSalidaDetalleEntity detalle) {
        if (detalle.pendiente() <= 0) {
            return List.of();
        }

        List<LoteEntity> lotes = detalle.getTipo() == TipoRecursoProcedimiento.MEDICAMENTO
                ? loteRepository.findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(
                        detalle.getReferenciaId(), true, 0)
                : loteRepository.findByInsumoLog_IdAndEstadoAndDisponibleGreaterThan(
                        detalle.getReferenciaId(), true, 0);

        LocalDate hoy = LocalDate.now();
        return lotes.stream()
                .filter(lote -> lote.getFechaVencimiento() == null
                        || !lote.getFechaVencimiento().isBefore(hoy))
                .sorted(Comparator
                        .comparing(LoteEntity::getFechaVencimiento,
                                Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                        .thenComparing(LoteEntity::getId,
                                Comparator.nullsLast(Comparator.<Long>naturalOrder())))
                .map(lote -> new SolicitudSalidaDTOs.LoteDisponibleResponse(
                        lote.getId(),
                        lote.getCodigoLote(),
                        lote.getFechaVencimiento(),
                        valor(lote.getDisponible())))
                .toList();
    }

    private List<SolicitudSalidaDTOs.LoteEntregadoResponse> lotesEntregados(
            List<SolicitudSalidaMovimientoEntity> movimientos) {
        Map<Long, List<SolicitudSalidaMovimientoEntity>> porLote = movimientos.stream()
                .filter(movimiento -> movimiento.getLote() != null)
                .collect(Collectors.groupingBy(
                        movimiento -> movimiento.getLote().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<SolicitudSalidaDTOs.LoteEntregadoResponse> respuesta = new ArrayList<>();
        for (List<SolicitudSalidaMovimientoEntity> grupo : porLote.values()) {
            LoteEntity lote = grupo.getFirst().getLote();
            int entregado = 0;
            int devuelto = 0;
            for (SolicitudSalidaMovimientoEntity movimiento : grupo) {
                if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.ENTREGA) {
                    entregado += valor(movimiento.getCantidad());
                } else if (movimiento.getTipoMovimiento() == TipoMovimientoSalida.DEVOLUCION) {
                    devuelto += valor(movimiento.getCantidad());
                }
            }
            int disponibleDevolver = entregado - devuelto;
            if (disponibleDevolver <= 0) {
                continue;
            }
            respuesta.add(new SolicitudSalidaDTOs.LoteEntregadoResponse(
                    lote.getId(),
                    lote.getCodigoLote(),
                    lote.getFechaVencimiento(),
                    entregado,
                    devuelto,
                    disponibleDevolver));
        }

        respuesta.sort(Comparator.comparing(
                SolicitudSalidaDTOs.LoteEntregadoResponse::codigoLote,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return respuesta;
    }

    private boolean coincide(SolicitudSalidaEntity solicitud, String filtro) {
        if (filtro == null) {
            return true;
        }
        return contiene(solicitud.getCodigo(), filtro)
                || contiene(nombrePaciente(solicitud), filtro)
                || contiene(tipoAtencion(solicitud), filtro)
                || contiene(nombreProcedimiento(solicitud), filtro);
    }

    private String normalizarFiltro(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim().toLowerCase(Locale.ROOT);
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private SolicitudSalidaDTOs.ResumenResponse toResumen(SolicitudSalidaEntity solicitud) {
        List<SolicitudSalidaDetalleEntity> activos = solicitud.getDetalles().stream()
                .filter(detalle -> detalle.getEstado() != EstadoDetalleSalida.CANCELADA)
                .toList();

        int medicamentos = (int) activos.stream()
                .filter(detalle -> detalle.getTipo() == TipoRecursoProcedimiento.MEDICAMENTO)
                .count();
        int insumos = (int) activos.stream()
                .filter(detalle -> detalle.getTipo() == TipoRecursoProcedimiento.INSUMO)
                .count();
        int pendientes = (int) activos.stream()
                .filter(detalle -> detalle.getEstado() != EstadoDetalleSalida.COMPLETADA)
                .count();

        return new SolicitudSalidaDTOs.ResumenResponse(
                solicitud.getId(),
                solicitud.getCodigo(),
                nombrePaciente(solicitud),
                tipoAtencion(solicitud),
                nombreProcedimiento(solicitud),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado(),
                medicamentos,
                insumos,
                pendientes);
    }

    private SolicitudSalidaDTOs.Response toResponse(SolicitudSalidaEntity solicitud) {
        List<SolicitudSalidaMovimientoEntity> movimientos = solicitud.getId() == null
                ? List.of()
                : movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(solicitud.getId());

        Map<Long, List<SolicitudSalidaMovimientoEntity>> porDetalle = movimientos.stream()
                .filter(movimiento -> movimiento.getDetalle() != null)
                .collect(Collectors.groupingBy(
                        movimiento -> movimiento.getDetalle().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<SolicitudSalidaDTOs.DetalleResponse> detalles = solicitud.getDetalles().stream()
                .filter(detalle -> detalle.getEstado() != EstadoDetalleSalida.CANCELADA)
                .map(detalle -> new SolicitudSalidaDTOs.DetalleResponse(
                        detalle.getId(),
                        detalle.getTipo(),
                        detalle.getReferenciaId(),
                        detalle.getNombre(),
                        valor(detalle.getCantidadSolicitada()),
                        valor(detalle.getCantidadEntregada()),
                        detalle.pendiente(),
                        detalle.getEstado(),
                        lotesDisponibles(detalle),
                        lotesEntregados(porDetalle.getOrDefault(detalle.getId(), List.of()))))
                .toList();

        return new SolicitudSalidaDTOs.Response(
                solicitud.getId(),
                solicitud.getCodigo(),
                nombrePaciente(solicitud),
                nombreUsuario(solicitud.getUsuarioSolicita()),
                tipoAtencion(solicitud),
                nombreProcedimiento(solicitud),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado(),
                detalles);
    }

    private String nombrePaciente(SolicitudSalidaEntity solicitud) {
        if (solicitud.getEpisodio() == null
                || solicitud.getEpisodio().getPaciente() == null
                || solicitud.getEpisodio().getPaciente().getPersona() == null) {
            return "";
        }
        String nombre = solicitud.getEpisodio().getPaciente().getPersona().getNombreCompleto();
        return nombre == null ? "" : nombre;
    }

    private String tipoAtencion(SolicitudSalidaEntity solicitud) {
        if (solicitud.getEpisodio() == null || solicitud.getEpisodio().getTipoAtencion() == null) {
            return null;
        }
        return solicitud.getEpisodio().getTipoAtencion().name();
    }

    private String nombreProcedimiento(SolicitudSalidaEntity solicitud) {
        if (solicitud.getProcedimientoMedico() == null
                || solicitud.getProcedimientoMedico().getProcedimiento() == null) {
            return "";
        }
        return solicitud.getProcedimientoMedico().getProcedimiento().getNombre();
    }

    private int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    private String clave(TipoRecursoProcedimiento tipo, Long referenciaId) {
        return tipo + "#" + referenciaId;
    }

    private void validarEntrega(SolicitudSalidaDTOs.EntregaRequest request) {
        if (request == null || request.usuarioId() == null
                || request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("La entrega es obligatoria");
        }
    }

    private void validarDevolucion(SolicitudSalidaDTOs.DevolucionRequest request) {
        if (request == null || request.usuarioId() == null
                || request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("La devolución es obligatoria");
        }
    }

    private record RecursoAgrupado(
            TipoRecursoProcedimiento tipo,
            Long referenciaId,
            String nombre,
            Integer cantidad
    ) {}

    private record LineaDirectaAgrupada(
            TipoRecursoProcedimiento tipo,
            Long referenciaId,
            Integer cantidad
    ) {}
}
