package com.example.demo.modules.usuarios.horarios;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository repository;
    private final HorarioMapper mapper;

    @Transactional
    public HorarioDTOs.Response crear(HorarioDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        validarNoDuplicado(null, nombre);

        boolean esRotativo = req.esRotativo() != null ? req.esRotativo() : false;

        validarDetallesPorTipo(esRotativo, req);

        HorarioEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setEsRotativo(esRotativo);
        entity.setCodigo(generarCodigoHorario());

        if (!esRotativo) {
            List<HorarioSemanalDetalleEntity> detalles = construirDetallesSemanales(req.semanalDetalles(), entity);
            entity.setSemanalDetalles(detalles);
            entity.setTurnoDetalle(null);
        } else {
            HorarioTurnoDetalleEntity turno = construirTurnoDetalle(req.turnoDetalle(), entity);
            entity.setTurnoDetalle(turno);
            entity.setSemanalDetalles(new ArrayList<>());
        }

        HorarioEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

    @Transactional
    public HorarioDTOs.Response actualizar(Long id, HorarioDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        HorarioEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        validarNoDuplicado(id, nombre);

        boolean esRotativo = req.esRotativo() != null ? req.esRotativo() : existente.isEsRotativo();

        validarDetallesPorTipo(esRotativo, req);

        existente.setNombre(nombre);
        existente.setEsRotativo(esRotativo);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());

        if (!esRotativo) {
            // limpiar turno previo
            existente.setTurnoDetalle(null);
            // reemplazar detalles semanales
            existente.getSemanalDetalles().clear();
            List<HorarioSemanalDetalleEntity> nuevos = construirDetallesSemanales(req.semanalDetalles(), existente);
            existente.getSemanalDetalles().addAll(nuevos);
        } else {
            // limpiar semanales previos
            existente.getSemanalDetalles().clear();
            HorarioTurnoDetalleEntity turno;
            if (existente.getTurnoDetalle() != null) {
                turno = existente.getTurnoDetalle();
                turno.setHorasTrabajo(req.turnoDetalle().horasTrabajo());
                turno.setHorasDescanso(req.turnoDetalle().horasDescanso());
            } else {
                turno = construirTurnoDetalle(req.turnoDetalle(), existente);
                existente.setTurnoDetalle(turno);
            }
        }

        HorarioEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public HorarioDTOs.Response obtenerPorId(Long id) {
        HorarioEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<HorarioDTOs.Response> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream()
                    .map(mapper::toDTO)
                    .toList();
        }
        return repository.findByEstado(activos).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HorarioDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }
        String nombreNormalizado = nombre.trim();
        if (activos == null) {
            return repository.findByNombreContainingIgnoreCase(nombreNormalizado).stream()
                    .map(mapper::toDTO)
                    .toList();
        }
        return repository.findByNombreContainingIgnoreCaseAndEstado(nombreNormalizado, activos).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public void cambiarEstado(Long id) {
        HorarioEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<HorarioEntity> duplicado = repository.findByNombreIgnoreCase(nombre);
        if (duplicado.isEmpty() || duplicado.get().getId().equals(idActual)) {
            return;
        }
        if (duplicado.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un horario activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException(
                "Ya existe un horario con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo."
        );
    }

    private void validarDetallesPorTipo(boolean esRotativo, HorarioDTOs.Request req) {
        if (!esRotativo) {
            if (req.semanalDetalles() == null || req.semanalDetalles().isEmpty()) {
                throw new IllegalArgumentException("Debe proporcionar al menos un detalle semanal cuando es_rotativo es false");
            }
            for (HorarioDTOs.DetalleSemanalRequest d : req.semanalDetalles()) {
                if (d.diaSemana() == null) {
                    throw new IllegalArgumentException("El día de semana es obligatorio");
                }
                boolean detalleActivo = d.activo() != null ? d.activo() : true;
                if (!detalleActivo) {
                    continue;
                }
                if (d.horaEntrada() == null || d.horaSalida() == null) {
                    throw new IllegalArgumentException("La hora de entrada y salida son obligatorias");
                }
                if (!d.horaEntrada().isBefore(d.horaSalida())) {
                    if (d.horaEntrada().equals(LocalTime.MIDNIGHT) && d.horaSalida().equals(LocalTime.MIDNIGHT)) {
                        continue;
                    }else{
                        throw new IllegalArgumentException("La hora de entrada debe ser anterior a la hora de salida");
                    }
                }
            }
        } else {
            if (req.turnoDetalle() == null) {
                throw new IllegalArgumentException("Debe proporcionar el detalle de turno cuando es_rotativo es true");
            }
            if (req.turnoDetalle().horasTrabajo() == null || req.turnoDetalle().horasTrabajo() <= 0) {
                throw new IllegalArgumentException("Las horas de trabajo deben ser mayor a 0");
            }
            if (req.turnoDetalle().horasDescanso() == null || req.turnoDetalle().horasDescanso() < 0) {
                throw new IllegalArgumentException("Las horas de descanso no pueden ser negativas");
            }
        }
    }

    private List<HorarioSemanalDetalleEntity> construirDetallesSemanales(List<HorarioDTOs.DetalleSemanalRequest> requests, HorarioEntity horario) {
    List<HorarioSemanalDetalleEntity> detalles = new ArrayList<>();
    for (HorarioDTOs.DetalleSemanalRequest r : requests) {
        boolean activo = r.activo() != null ? r.activo() : true;

        if (r.horaEntrada() != null && r.horaSalida() != null
                && r.horaEntrada().equals(LocalTime.MIDNIGHT)
                && r.horaSalida().equals(LocalTime.MIDNIGHT)) {
            activo = false;
        }

        HorarioSemanalDetalleEntity e = HorarioSemanalDetalleEntity.builder()
                .horario(horario)
                .diaSemana(r.diaSemana())
                .horaEntrada(r.horaEntrada())
                .horaSalida(r.horaSalida())
                .activo(activo)
                .build();
        detalles.add(e);
    }
    return detalles;
}

    private HorarioTurnoDetalleEntity construirTurnoDetalle(HorarioDTOs.TurnoDetalleRequest request, HorarioEntity horario) {
        return HorarioTurnoDetalleEntity.builder()
                .horario(horario)
                .horasTrabajo(request.horasTrabajo())
                .horasDescanso(request.horasDescanso())
                .build();
    }

    private String generarCodigoHorario() {
        long total = repository.count();
        return String.format("HOR-%02d", total + 1);
    }
}
