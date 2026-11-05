package com.example.demo.modules.farmacia.insumosLog;

import com.example.demo.config.ConflictException;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.security.UsuarioActualService;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class insumosLogService {
    private final insumosLogRepository repository;
    private final marcaRepository marcaRepository;
    private final LoteRepository loteRepository;
    private final procedimientoMedicoRepository procedimientoMedicoRepository;
    private final insumoCorreccionRepository correccionRepository;
    private final UsuarioActualService usuarioActualService;

    @Transactional
    public insumosLogDTOs.Response crear(insumosLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("La solicitud es obligatoria");
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        validarNombreUnico(null, nombre);
        insumosLogEntity entity = insumosLogEntity.builder()
            .nombre(nombre)
            .codigo(generarCodigo())
            .detalle(StringNormalizer.normalizarNullable(request.detalle()))
            .build();
        asignarMarca(entity, request.marcaId());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public insumosLogDTOs.Response actualizar(Long id, insumosLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("Sin datos para actualizar");
        insumosLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        if (tieneHistorial(id) && identidadCambiada(entity, request, nombre)) {
            throw new ConflictException("El insumo tiene historial y no se puede modificar su identidad. "
                    + "Corrija el nombre mediante la corrección justificada.");
        }
        validarNombreUnico(id, nombre);
        entity.setNombre(nombre);
        entity.setDetalle(StringNormalizer.normalizarNullable(request.detalle()));
        asignarMarca(entity, request.marcaId());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public insumosLogDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<insumosLogDTOs.ListadoResponse> obtenerTodos(boolean activo) {
        return repository.findByEstado(activo).stream().map(this::toListadoResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<insumosLogDTOs.ListadoResponse> buscar(String nombre, Long marcaId, boolean activo) {
        String nombreNormalizado = StringNormalizer.normalizarNullable(nombre);
        if (nombreNormalizado == null && marcaId == null) return List.of();

        List<insumosLogEntity> resultados;
        if (nombreNormalizado != null && marcaId != null) {
            resultados = repository.findByNombreContainingIgnoreCaseAndMarca_IdAndEstado(
                    nombreNormalizado, marcaId, activo);
        } else if (nombreNormalizado != null) {
            resultados = repository.findByNombreContainingIgnoreCaseAndEstado(nombreNormalizado, activo);
        } else {
            resultados = repository.findByMarca_IdAndEstado(marcaId, activo);
        }
        return resultados.stream().map(this::toListadoResponse).toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        insumosLogEntity entity = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
        if (entity.isEstado()) {
            long existencias = existenciasInsumo(id);
            if (existencias > 0) {
                throw new ConflictException("No se puede deshabilitar el insumo: existen "
                        + existencias + " unidades pendientes en farmacia.");
            }
        }
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    @Transactional
    public insumosLogDTOs.Response corregir(Long id, insumosLogDTOs.CorreccionRequest request) {
        if (request == null) throw new IllegalArgumentException("La solicitud de corrección es obligatoria");
        insumosLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con el ID: " + id));
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
        validarNombreUnico(id, nombreNuevo);
        UsuarioEntity usuario = usuarioActualService.actual()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo identificar al usuario que realiza la corrección"));
        correccionRepository.save(insumoCorreccionEntity.builder()
                .insumoLog(entity)
                .nombreAnterior(entity.getNombre())
                .nombreNuevo(nombreNuevo)
                .motivo(motivo)
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .build());
        entity.setNombre(nombreNuevo);
        return toResponse(repository.save(entity));
    }

    /** Existe historial cuando el insumo ya fue usado en lotes o procedimientos. */
    private boolean tieneHistorial(Long id) {
        return loteRepository.existsByInsumoLog_Id(id)
                || procedimientoMedicoRepository.existsByRecursos_TipoAndRecursos_ReferenciaId(
                        TipoRecursoProcedimiento.INSUMO, id);
    }

    /** Compara los campos que forman la identidad del insumo (nombre, marca y detalle). */
    private boolean identidadCambiada(insumosLogEntity entity, insumosLogDTOs.Request request, String nombre) {
        String detalle = StringNormalizer.normalizarNullable(request.detalle());
        return !Objects.equals(entity.getNombre(), nombre)
                || !Objects.equals(entity.getMarca().getId(), request.marcaId())
                || !Objects.equals(entity.getDetalle(), detalle);
    }

    private long existenciasInsumo(Long id) {
        Long total = loteRepository.sumExistenciasInsumo(id);
        return total == null ? 0 : total;
    }

    private void asignarMarca(insumosLogEntity entity, Long marcaId) {
        entity.setMarca(marcaRepository.findById(marcaId)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con el ID: " + marcaId)));
    }

    private void validarNombreUnico(Long idActual, String nombre) {
        repository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un insumo con el nombre: " + nombre);
            }
        });
    }

    private insumosLogDTOs.Response toResponse(insumosLogEntity entity) {
        return new insumosLogDTOs.Response(entity.getId(), entity.getNombre(), entity.getCodigo(), entity.getDetalle(),
            entity.getMarca().getId(), entity.getMarca().getNombre());
    }

    private insumosLogDTOs.ListadoResponse toListadoResponse(insumosLogEntity entity) {
        return new insumosLogDTOs.ListadoResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getCodigo(),
                entity.getDetalle(),
                new insumosLogDTOs.MarcaResponse(entity.getMarca().getNombre(), entity.getMarca().getId()),
                entity.isEstado());
    }

    private String generarCodigo() {
        long numero = repository.count() + 1;
        String codigo = String.format("INS-%d", numero);
        while (repository.existsByCodigo(codigo)) {
            numero++;
            codigo = String.format("INS-%d", numero);
        }
        return codigo;
    }
}