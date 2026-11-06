package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.config.ConflictException;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoRepository;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaRepository;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.security.UsuarioActualService;
import com.example.demo.utils.StringNormalizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class medicamentoLogService {
    private final medicamentoLogRepository repository;
    private final unidadMedidaRepository unidadMedidaRepository;
    private final marcaRepository marcaRepository;
    private final viaAdminRepository viaAdminRepository;
    private final presentacionRepository presentacionRepository;
    private final motivoBajaRepository motivoBajaRepository;
    private final LoteRepository loteRepository;
    private final indicacionMedicamentoRepository indicacionMedicamentoRepository;
    private final procedimientoMedicoRepository procedimientoMedicoRepository;
    private final medicamentoCorreccionRepository correccionRepository;
    private final UsuarioActualService usuarioActualService;

    public Map<String, Long> obtenerConteoCatalogosFarmacia() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("unidad_medidas", unidadMedidaRepository.count());
        conteo.put("marcas", marcaRepository.count());
        conteo.put("vias_admin", viaAdminRepository.count());
        conteo.put("presentaciones", presentacionRepository.count());
        conteo.put("motivoBaja", motivoBajaRepository.count());
        return conteo;
    }

    @Transactional
    public medicamentoLogDTOs.Response crear(medicamentoLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("La solicitud es obligatoria");
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        BigDecimal dosis = request.dosis();
        medicamentoLogEntity entity = construir(request, nombre, dosis);
        validarNoDuplicado(null, entity);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public medicamentoLogDTOs.Response actualizar(Long id, medicamentoLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("Sin datos para actualizar");
        medicamentoLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        BigDecimal dosis = request.dosis();
        if (tieneHistorial(id) && identidadCambiada(entity, request, nombre, dosis)) {
            throw new ConflictException("El medicamento tiene historial y no se puede modificar su identidad. "
                    + "Corrija el nombre mediante la corrección justificada.");
        }
        entity.setNombre(nombre);
        entity.setDosis(dosis);
        asignarRelaciones(entity, request);
        validarNoDuplicado(id, entity);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public medicamentoLogDTOs.Response obtenerPorId(Long id, boolean activo) {
        return repository.findByIdAndEstado(id, activo).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<medicamentoLogDTOs.MedicamentoLogResponse> obtenerTodos(boolean activo) {
        return repository.findByEstado(activo).stream().map(this::toListadoResponse).toList();
    }

    @Transactional(readOnly = true)
    public medicamentoLogDTOs.MedicamentoLogBusquedaResponse buscarPorId(Long id, boolean activo) {
        return repository.findByIdAndEstado(id, activo).map(this::toBusquedaResponse)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<medicamentoLogDTOs.MedicamentoLogResponse> buscar(
            String nombre, Long marcaId, Long presentacionId, Long viaAdminId, boolean activo) {
        String nombreNormalizado = StringNormalizer.normalizarNullable(nombre);
        if (nombreNormalizado == null && marcaId == null && presentacionId == null && viaAdminId == null) {
            return List.of();
        }
        return repository.buscar(nombreNormalizado, marcaId, presentacionId, viaAdminId, activo)
                .stream().map(this::toListadoResponse).toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        medicamentoLogEntity entity = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
        if (entity.isEstado()) {
            long existencias = existenciasMedicamento(id);
            if (existencias > 0) {
                throw new ConflictException("No se puede deshabilitar el medicamento: existen "
                        + existencias + " unidades pendientes en farmacia.");
            }
        }
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    @Transactional
    public medicamentoLogDTOs.Response corregir(Long id, medicamentoLogDTOs.CorreccionRequest request) {
        if (request == null) throw new IllegalArgumentException("La solicitud de corrección es obligatoria");
        medicamentoLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
        String nombreNuevo = StringNormalizer.normalizarTexto(request.nombre());
        if (nombreNuevo.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String motivo = StringNormalizer.normalizarTexto(request.motivo());
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la corrección es obligatorio");
        }
        if (nombreNuevo.equalsIgnoreCase(entity.getNombre())) {
            throw new IllegalArgumentException("El nombre nuevo debe ser diferente al actual");
        }
        medicamentoLogEntity candidato = medicamentoLogEntity.builder()
                .nombre(nombreNuevo)
                .dosis(entity.getDosis())
                .unidadMedida(entity.getUnidadMedida())
                .marca(entity.getMarca())
                .viaAdmin(entity.getViaAdmin())
                .presentacion(entity.getPresentacion())
                .build();
        validarNoDuplicado(id, candidato);
        UsuarioEntity usuario = usuarioActualService.actual()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo identificar al usuario que realiza la corrección"));
        correccionRepository.save(medicamentoCorreccionEntity.builder()
                .medicamentoLog(entity)
                .nombreAnterior(entity.getNombre())
                .nombreNuevo(nombreNuevo)
                .motivo(motivo)
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .build());
        entity.setNombre(nombreNuevo);
        return toResponse(repository.save(entity));
    }

    private medicamentoLogEntity construir(medicamentoLogDTOs.Request request, String nombre, BigDecimal dosis) {
        medicamentoLogEntity entity = medicamentoLogEntity.builder()
                .nombre(nombre)
                .codigo(generarCodigo())
                .dosis(dosis).build();
        asignarRelaciones(entity, request);
        return entity;
    }

    private String generarCodigo() {
        long numero = repository.count() + 1;
        String codigo = String.format("MED-%d", numero);
        while (repository.existsByCodigo(codigo)) {
            numero++;
            codigo = String.format("MED-%d", numero);
        }
        return codigo;
    }

    private void asignarRelaciones(medicamentoLogEntity entity, medicamentoLogDTOs.Request request) {
        entity.setUnidadMedida(unidadMedidaRepository.findById(request.unidadMedidaId())
                .orElseThrow(() -> new IllegalArgumentException("Unidad de medida no encontrada con el ID: " + request.unidadMedidaId())));
        entity.setMarca(marcaRepository.findById(request.marcaId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con el ID: " + request.marcaId())));
        entity.setViaAdmin(viaAdminRepository.findById(request.viaAdminId())
                .orElseThrow(() -> new IllegalArgumentException("Vía de administración no encontrada con el ID: " + request.viaAdminId())));
        entity.setPresentacion(presentacionRepository.findById(request.presentacionId())
                .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada con el ID: " + request.presentacionId())));
    }

    private void validarNoDuplicado(Long idActual, medicamentoLogEntity medicamento) {
        repository.findByIdentidad(
                medicamento.getNombre(),
                medicamento.getDosis(),
                medicamento.getUnidadMedida().getId(),
                medicamento.getMarca().getId(),
                medicamento.getViaAdmin().getId(),
                medicamento.getPresentacion().getId()
        ).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un medicamento con los mismos datos");
            }
        });
    }

    /** Existe historial cuando el medicamento ya fue usado en lotes, indicaciones o procedimientos. */
    private boolean tieneHistorial(Long id) {
        return loteRepository.existsByMedicamentoLog_Id(id)
                || indicacionMedicamentoRepository.existsByMedicamentoLog_Id(id)
                || procedimientoMedicoRepository.existsByRecursos_TipoAndRecursos_ReferenciaId(
                        TipoRecursoProcedimiento.MEDICAMENTO, id);
    }

    /** Compara los campos que forman la identidad del medicamento (los que no son el nombre). */
    private boolean identidadCambiada(
            medicamentoLogEntity entity, medicamentoLogDTOs.Request request, String nombre, BigDecimal dosis) {
        return !Objects.equals(entity.getNombre(), nombre)
                || entity.getDosis().compareTo(dosis) != 0
                || !Objects.equals(entity.getUnidadMedida().getId(), request.unidadMedidaId())
                || !Objects.equals(entity.getMarca().getId(), request.marcaId())
                || !Objects.equals(entity.getViaAdmin().getId(), request.viaAdminId())
                || !Objects.equals(entity.getPresentacion().getId(), request.presentacionId());
    }

    private long existenciasMedicamento(Long id) {
        Long total = loteRepository.sumExistenciasMedicamento(id);
        return total == null ? 0 : total;
    }

    private medicamentoLogDTOs.Response toResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.Response(entity.getId(), entity.getNombre(), entity.getCodigo(), entity.getDosis(),
                entity.getUnidadMedida().getId(), entity.getUnidadMedida().getNombre(), entity.getMarca().getId(),
                entity.getMarca().getNombre(), entity.getViaAdmin().getId(), entity.getViaAdmin().getNombre(),
                entity.getPresentacion().getId(), entity.getPresentacion().getNombre());
    }

    private medicamentoLogDTOs.MedicamentoLogResponse toListadoResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.MedicamentoLogResponse(entity.getId(), entity.getNombre(), entity.getCodigo(), entity.getDosis(),
                entity.getMarca().getNombre(), entity.getPresentacion().getNombre(),
            entity.getViaAdmin().getNombre(), entity.getUnidadMedida().getAbreviatura(),
            disponibleMedicamento(entity.getId()), entity.isEstado());
    }

    /** Unidades disponibles del medicamento en lotes activos y vigentes. */
    private Integer disponibleMedicamento(Long medicamentoLogId) {
        Long total = loteRepository.sumDisponibleMedicamento(medicamentoLogId, LocalDate.now());
        return total == null ? 0 : Math.toIntExact(total);
    }

    private medicamentoLogDTOs.MedicamentoLogBusquedaResponse toBusquedaResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.MedicamentoLogBusquedaResponse(entity.getNombre(), entity.getDosis(),
                entity.getUnidadMedida().getId(), entity.getViaAdmin().getId(),
                entity.getPresentacion().getId(), entity.getMarca().getId());
    }

}