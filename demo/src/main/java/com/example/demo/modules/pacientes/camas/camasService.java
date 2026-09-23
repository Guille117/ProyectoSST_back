package com.example.demo.modules.pacientes.camas;

import com.example.demo.modules.pacientes.area.areaRepository;
import com.example.demo.modules.pacientes.habitaciones.habitacionesRepository;
import com.example.demo.modules.pacientes.tipoCama.tipoCamaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class camasService {
    private final camasRepository repository;
    private final habitacionesRepository habitacionesRepository;
    private final tipoCamaRepository tipoCamaRepository;
    private final areaRepository areaRepository;

    public camasService(camasRepository repository, habitacionesRepository habitacionesRepository,
                        tipoCamaRepository tipoCamaRepository, areaRepository areaRepository) {
        this.repository = repository;
        this.habitacionesRepository = habitacionesRepository;
        this.tipoCamaRepository = tipoCamaRepository;
        this.areaRepository = areaRepository;
    }

    @Transactional
    public camasDTOs.Response crear(camasDTOs.Request request) {
        camasEntity cama = construirEntidad(request, null);
        cama.setCodigo(generarCodigo());
        return toResponse(repository.save(cama));
    }

    @Transactional(readOnly = true)
    public List<camasDTOs.ListResponse> obtenerTodos(Boolean activos) {
        List<camasEntity> camas = activos == null ? repository.findAll() : repository.findByActivo(activos);
        return camas.stream().map(this::toListResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<camasDTOs.ListResponse> buscar(String texto, Boolean activos) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de búsqueda es obligatorio");
        }
        String textoNormalizado = texto.trim();
        EstadoCama estado = null;
        try {
            estado = EstadoCama.valueOf(textoNormalizado.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // El texto corresponde a código o nombre de un catálogo.
        }

        return repository.buscar(textoNormalizado, activos, estado).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public camasDTOs.Response obtenerPorId(Long id) {
        camasEntity cama = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrada con el ID: " + id));
        return toResponse(cama);
    }

        @Transactional(readOnly = true)
        public camasDTOs.ReferenciasResponse obtenerReferencias(Long id) {
        camasEntity cama = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cama no encontrada con el ID: " + id));
        return new camasDTOs.ReferenciasResponse(
            cama.getHabitacion().getId(),
            cama.getArea().getId(),
            cama.getTipo().getId());
        }

    @Transactional
    public camasDTOs.Response actualizar(Long id, camasDTOs.Request request) {
        camasEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrada con el ID: " + id));
        camasEntity actualizada = construirEntidad(request, existente);
        existente.setHabitacion(actualizada.getHabitacion());
        existente.setTipo(actualizada.getTipo());
        existente.setArea(actualizada.getArea());
        // el estado solo se cambia si el request lo trae explícito; si no, se conserva el actual
        existente.setEstado(request.estado() != null ? request.estado() : existente.getEstado());
        existente.setActivo(actualizada.isActivo());
        return toResponse(repository.save(existente));
    }

    @Transactional
    public camasDTOs.Response cambiarEstado(Long id) {
        camasEntity cama = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrada con el ID: " + id));
        cama.setActivo(!cama.isActivo());
        return toResponse(repository.save(cama));
    }

    // función para cambiar de estado de cama posibles estados (DISPONIBLE, OCUPADA, MANTENIMIENTO)
    @Transactional
    public void cambiarEstadoCama(Long id, EstadoCama nuevoEstado) {
        camasEntity cama = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cama no encontrada con el ID: " + id));
        cama.setEstado(nuevoEstado);
        repository.save(cama);
    }
    
    private camasEntity construirEntidad(camasDTOs.Request request, camasEntity existente) {
        camasEntity entidad = new camasEntity();
        entidad.setHabitacion(habitacionesRepository.findById(request.habitacionId())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada con el ID: " + request.habitacionId())));
        entidad.setTipo(tipoCamaRepository.findById(request.tipoId())
                .orElseThrow(() -> new RuntimeException("Tipo de cama no encontrado con el ID: " + request.tipoId())));
        entidad.setArea(areaRepository.findById(request.areaId())
                .orElseThrow(() -> new RuntimeException("Área no encontrada con el ID: " + request.areaId())));
        entidad.setEstado(request.estado() != null ? request.estado() : EstadoCama.DISPONIBLE);
        entidad.setActivo(request.activo() != null ? request.activo() : existente == null || existente.isActivo());
        return entidad;
    }

    private String generarCodigo() {
        long siguiente = repository.count() + 1;
        String codigo;
        do {
            codigo = String.format("CAMA-%02d", siguiente++);
        } while (repository.existsByCodigo(codigo));
        return codigo;
    }

    private camasDTOs.Response toResponse(camasEntity cama) {
        return new camasDTOs.Response(cama.getId(), cama.getCodigo(),
                new camasDTOs.CatalogoResponse(cama.getHabitacion().getId(), cama.getHabitacion().getNombre()),
                new camasDTOs.CatalogoResponse(cama.getTipo().getId(), cama.getTipo().getNombre()),
                new camasDTOs.CatalogoResponse(cama.getArea().getId(), cama.getArea().getNombre()),
                cama.getEstado(), cama.isActivo());
    }

    private camasDTOs.ListResponse toListResponse(camasEntity cama) {
        return new camasDTOs.ListResponse(cama.getId(), cama.getCodigo(), cama.getEstado(),
            cama.getHabitacion().getNombre(), cama.getArea().getNombre(), cama.getTipo().getNombre(),
            cama.isActivo());
    }
}