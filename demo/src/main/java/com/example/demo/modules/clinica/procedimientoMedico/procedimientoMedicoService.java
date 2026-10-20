package com.example.demo.modules.clinica.procedimientoMedico;

import com.example.demo.modules.clinica.procedimiento.procedimientoEntity;
import com.example.demo.modules.clinica.procedimiento.procedimientoRepository;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class procedimientoMedicoService {

    private final procedimientoMedicoRepository repository;
    private final procedimientoRepository procedimientoRepository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final medicamentoLogRepository medicamentoLogRepository;
    private final insumosLogRepository insumosLogRepository;
    private final LoteRepository loteRepository;
    private final SolicitudSalidaService solicitudSalidaService;

    @Transactional
    public procedimientoMedicoDTOs.Response crear(procedimientoMedicoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        procedimientoEntity tipo = buscarTipoProcedimiento(request.tipoProcedimientoId(), null);
        EpisodioEntity episodio = buscarEpisodioAbierto(request.episodioId());
        UsuarioEntity usuario = buscarUsuario(request.usuarioId());
        List<procedimientoMedicoRecursoEntity> recursos =
                resolverRecursos(request.recursos(), List.of(), Map.of());

        procedimientoMedicoEntity entity = procedimientoMedicoEntity.builder()
                .procedimiento(tipo)
                .descripcion(StringNormalizer.normalizarTexto(request.descripcion()))
                .resultado(StringNormalizer.normalizarNullable(request.resultado()))
                .inicio(request.inicio())
                .fin(request.fin())
                .episodio(episodio)
                .usuario(usuario)
                .build();
        entity.setEstado(calcularEstado(entity.getResultado(), entity.getFin()));
        recursos.forEach(entity::agregarRecurso);

        procedimientoMedicoEntity guardado = repository.save(entity);
        solicitudSalidaService.sincronizar(guardado);
        return toResponse(guardado);
    }

    @Transactional
    public procedimientoMedicoDTOs.Response actualizar(Long id, procedimientoMedicoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        procedimientoMedicoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Procedimiento médico no encontrado con el ID: " + id));

        validarEpisodioYUsuarioSinCambios(entity, request);
        validarEpisodioAbierto(entity.getEpisodio());

        procedimientoEntity tipo = buscarTipoProcedimiento(
                request.tipoProcedimientoId(), entity.getProcedimiento().getId());
        List<procedimientoMedicoRecursoEntity> recursos = resolverRecursos(
                request.recursos(),
                entity.getRecursos(),
                solicitudSalidaService.obtenerCantidadesEntregadas(id));

        entity.setProcedimiento(tipo);
        entity.setDescripcion(StringNormalizer.normalizarTexto(request.descripcion()));
        entity.setResultado(StringNormalizer.normalizarNullable(request.resultado()));
        entity.setInicio(request.inicio());
        entity.setFin(request.fin());

        if (entity.getEstado() != EstadoProcedimientoMedico.ELIMINADO) {
            entity.setEstado(calcularEstado(entity.getResultado(), entity.getFin()));
        }

        entity.getRecursos().clear();
        recursos.forEach(entity::agregarRecurso);

        procedimientoMedicoEntity guardado = repository.save(entity);
        solicitudSalidaService.sincronizar(guardado);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public procedimientoMedicoDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Procedimiento médico no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<procedimientoMedicoDTOs.Response> buscarPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdOrderByInicioDesc(episodioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        procedimientoMedicoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Procedimiento médico no encontrado con el ID: " + id));

        validarEpisodioAbierto(entity.getEpisodio());
        solicitudSalidaService.cancelarPorProcedimiento(id);
        entity.setEstado(EstadoProcedimientoMedico.ELIMINADO);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<procedimientoMedicoDTOs.RecursoDisponibleResponse> buscarRecursos(
            TipoRecursoProcedimiento tipo, String nombre) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }

        String filtro = StringNormalizer.normalizarNullable(nombre);

        if (tipo == TipoRecursoProcedimiento.MEDICAMENTO) {
            List<medicamentoLogEntity> medicamentos = filtro == null
                    ? medicamentoLogRepository.findByEstado(true)
                    : medicamentoLogRepository.buscarDisponibles(filtro);

            return medicamentos.stream()
                    .map(medicamento -> new procedimientoMedicoDTOs.RecursoDisponibleResponse(
                            medicamento.getId(),
                            nombreMedicamento(medicamento),
                            disponibleMedicamento(medicamento.getId())))
                    .filter(recurso -> recurso.disponible() > 0)
                    .toList();
        }

        List<insumosLogEntity> insumos = filtro == null
                ? insumosLogRepository.findByEstado(true)
                : insumosLogRepository.findByNombreContainingIgnoreCaseAndEstado(filtro, true);

        return insumos.stream()
                .map(insumo -> new procedimientoMedicoDTOs.RecursoDisponibleResponse(
                        insumo.getId(),
                        insumo.getNombre(),
                        disponibleInsumo(insumo.getId())))
                .filter(recurso -> recurso.disponible() > 0)
                .toList();
    }

    private List<procedimientoMedicoRecursoEntity> resolverRecursos(
            List<procedimientoMedicoDTOs.RecursoRequest> solicitudes,
            List<procedimientoMedicoRecursoEntity> existentes,
            Map<String, Integer> entregados) {
        if (solicitudes == null) {
            throw new IllegalArgumentException("Los recursos son obligatorios");
        }

        Map<String, procedimientoMedicoDTOs.RecursoRequest> agrupados = new LinkedHashMap<>();

        for (procedimientoMedicoDTOs.RecursoRequest solicitud : solicitudes) {
            if (solicitud == null || solicitud.tipo() == null || solicitud.referenciaId() == null) {
                throw new IllegalArgumentException("Los datos del recurso son obligatorios");
            }
            if (solicitud.cantidad() == null || solicitud.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            String clave = solicitud.tipo() + "#" + solicitud.referenciaId();
            procedimientoMedicoDTOs.RecursoRequest previa = agrupados.get(clave);
            if (previa == null) {
                agrupados.put(clave, solicitud);
            } else {
                agrupados.put(clave, new procedimientoMedicoDTOs.RecursoRequest(
                        solicitud.tipo(),
                        solicitud.referenciaId(),
                        Math.addExact(previa.cantidad(), solicitud.cantidad())));
            }
        }

        List<procedimientoMedicoRecursoEntity> recursos = new ArrayList<>();

        for (procedimientoMedicoDTOs.RecursoRequest solicitud : agrupados.values()) {
            procedimientoMedicoRecursoEntity existente = buscarRecursoExistente(existentes, solicitud);
            String clave = solicitud.tipo() + "#" + solicitud.referenciaId();
            int yaEntregado = entregados == null ? 0 : entregados.getOrDefault(clave, 0);

            // Los recursos históricos no se revalidan contra el stock actual si no aumentan.
            if (existente != null && solicitud.cantidad() <= existente.getCantidad()) {
                recursos.add(nuevoRecurso(solicitud, existente.getNombre()));
                continue;
            }

            // Solo debe existir stock para lo que aún falta entregar.
            int pendiente = solicitud.cantidad() - yaEntregado;

            if (solicitud.tipo() == TipoRecursoProcedimiento.MEDICAMENTO) {
                medicamentoLogEntity medicamento = medicamentoLogRepository
                        .findByIdAndEstado(solicitud.referenciaId(), true)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Medicamento no encontrado con el ID: " + solicitud.referenciaId()));
                String nombre = nombreMedicamento(medicamento);
                if (pendiente > 0) {
                    validarCantidad(pendiente, disponibleMedicamento(medicamento.getId()), nombre);
                }
                recursos.add(nuevoRecurso(solicitud, nombre));
                continue;
            }

            insumosLogEntity insumo = insumosLogRepository.findById(solicitud.referenciaId())
                    .filter(insumosLogEntity::isEstado)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Insumo no encontrado con el ID: " + solicitud.referenciaId()));
            if (pendiente > 0) {
                validarCantidad(pendiente, disponibleInsumo(insumo.getId()), insumo.getNombre());
            }
            recursos.add(nuevoRecurso(solicitud, insumo.getNombre()));
        }

        return recursos;
    }

    private procedimientoMedicoRecursoEntity buscarRecursoExistente(
            List<procedimientoMedicoRecursoEntity> existentes,
            procedimientoMedicoDTOs.RecursoRequest solicitud) {
        if (existentes == null) {
            return null;
        }

        return existentes.stream()
                .filter(recurso -> recurso.getTipo() == solicitud.tipo()
                        && solicitud.referenciaId().equals(recurso.getReferenciaId()))
                .findFirst()
                .orElse(null);
    }

    private procedimientoMedicoRecursoEntity nuevoRecurso(
            procedimientoMedicoDTOs.RecursoRequest solicitud, String nombre) {
        return procedimientoMedicoRecursoEntity.builder()
                .tipo(solicitud.tipo())
                .referenciaId(solicitud.referenciaId())
                .nombre(nombre)
                .cantidad(solicitud.cantidad())
                .build();
    }

    private void validarCantidad(Integer cantidad, int disponible, String nombre) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (cantidad > disponible) {
            throw new IllegalArgumentException(
                    "La cantidad pendiente de " + nombre + " supera la disponible (" + disponible + ")");
        }
    }

    private procedimientoEntity buscarTipoProcedimiento(Long id, Long idPermitidoInactivo) {
        procedimientoEntity tipo = procedimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de procedimiento no encontrado con el ID: " + id));

        if (!tipo.isEstado() && !tipo.getId().equals(idPermitidoInactivo)) {
            throw new IllegalArgumentException(
                    "El tipo de procedimiento está inactivo: " + tipo.getNombre());
        }

        return tipo;
    }

    private EpisodioEntity buscarEpisodioAbierto(Long episodioId) {
        EpisodioEntity episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episodio no encontrado con el ID: " + episodioId));
        validarEpisodioAbierto(episodio);
        return episodio;
    }

    private void validarEpisodioAbierto(EpisodioEntity episodio) {
        if (episodio == null || !episodio.isEstado()) {
            throw new IllegalArgumentException(
                    "El episodio está cerrado y no permite modificar procedimientos.");
        }
    }

    private void validarEpisodioYUsuarioSinCambios(
            procedimientoMedicoEntity entity, procedimientoMedicoDTOs.Request request) {
        if (!entity.getEpisodio().getId().equals(request.episodioId())
                || !entity.getUsuario().getId().equals(request.usuarioId())) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el episodio ni el usuario de un procedimiento.");
        }
    }

    private UsuarioEntity buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + usuarioId));
    }

    private EstadoProcedimientoMedico calcularEstado(String resultado, LocalDateTime fin) {
        return resultado != null && fin != null
                ? EstadoProcedimientoMedico.REALIZADO
                : EstadoProcedimientoMedico.PENDIENTE;
    }

    private int disponibleMedicamento(Long medicamentoLogId) {
        Long total = loteRepository.sumDisponibleMedicamento(medicamentoLogId, LocalDate.now());
        return total == null ? 0 : Math.toIntExact(total);
    }

    private int disponibleInsumo(Long insumoLogId) {
        Long total = loteRepository.sumDisponibleInsumo(insumoLogId, LocalDate.now());
        return total == null ? 0 : Math.toIntExact(total);
    }

    private String nombreMedicamento(medicamentoLogEntity medicamento) {
        String nombre = Stream.of(
                        medicamento.getNombre(),
                        medicamento.getMarca() == null ? null : medicamento.getMarca().getNombre(),
                        medicamento.getPresentacion() == null ? null : medicamento.getPresentacion().getNombre())
                .filter(parte -> parte != null && !parte.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));

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

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        String nombreCompleto = usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : "";
        return nombreCompleto.isBlank() ? usuario.getUsername() : nombreCompleto;
    }

    private procedimientoMedicoDTOs.Response toResponse(procedimientoMedicoEntity entity) {
        List<procedimientoMedicoDTOs.RecursoResponse> recursos = entity.getRecursos().stream()
                .map(recurso -> new procedimientoMedicoDTOs.RecursoResponse(
                        recurso.getTipo(),
                        recurso.getReferenciaId(),
                        recurso.getNombre(),
                        recurso.getCantidad()))
                .toList();

        return new procedimientoMedicoDTOs.Response(
                entity.getId(),
                entity.getProcedimiento().getId(),
                entity.getProcedimiento().getNombre(),
                entity.getDescripcion(),
                entity.getResultado(),
                entity.getInicio(),
                entity.getFin(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getEstado(),
                recursos);
    }
}
