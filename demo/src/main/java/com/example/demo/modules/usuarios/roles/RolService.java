package com.example.demo.modules.usuarios.roles;

import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository repository;
    private final RolMapper mapper;
    private final ModuloRepository moduloRepository;
    private final SubmoduloRepository submoduloRepository;

    @Transactional
    public RolDTOs.Response crear(RolDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        validarNoDuplicado(null, nombre);

        RolEntity entity = mapper.toEntity(req);
        entity.setNombre(nombre);
        entity.setCodigo(generarCodigoRol());

        if (req.permisos() != null && !req.permisos().isEmpty()) {
            List<RolPermisoEntity> permisos = construirPermisos(req.permisos(), entity);
            entity.setPermisos(permisos);
        } else {
            entity.setPermisos(new ArrayList<>());
        }

        RolEntity guardado = repository.save(entity);
        return mapper.toDTO(guardado);
    }

@Transactional
    public RolDTOs.Response actualizar(Long id, RolDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        RolEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String nombre = StringNormalizer.normalizarTexto(req.nombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        validarNoDuplicado(id, nombre);

        existente.setNombre(nombre);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());

        // --- GESTIÓN DE PERMISOS SIN CHOQUE DE CLAVE ÚNICA ---
        if (req.permisos() != null) {
            // 1. Mapear los permisos actuales por submodulo_id para búsqueda rápida O(1)
            Map<Long, RolPermisoEntity> actualesPorSubmodulo = existente.getPermisos().stream()
                    .collect(Collectors.toMap(
                            p -> p.getSubmodulo().getId(), 
                            p -> p,
                            (p1, p2) -> p1 // Evita error si hubiese inconsistencia en memoria
                    ));

            // 2. Set para saber qué submódulos vienen en la petición
            Set<Long> submodulosEnRequest = new HashSet<>();

            for (var dto : req.permisos()) {
                Long submoduloId = dto.submoduloId(); // O dto.getSubmoduloId() según tu record/clase
                submodulosEnRequest.add(submoduloId);

                RolPermisoEntity entidad = actualesPorSubmodulo.get(submoduloId);

                if (entidad != null) {
                    // YA EXISTE -> Solo se actualizan los booleanos (hace UPDATE en SQL, nunca choca)
                    entidad.setPuedeCrear(dto.puedeCrear());
                    entidad.setPuedeEditar(dto.puedeEditar());
                    entidad.setPuedeEliminar(dto.puedeEliminar());
                    entidad.setPuedeLeer(dto.puedeLeer());
                } else {
                    // NO EXISTE -> Se instancia y se añade a la colección (hace INSERT)
                    // (Usa tu método o constructor equivalente)
                    SubmoduloEntity submodulo = submoduloRepository.findById(submoduloId)
                            .orElseThrow(() -> new RuntimeException("Submódulo no encontrado: " + submoduloId));
                    
                    RolPermisoEntity nuevo = new RolPermisoEntity();
                    nuevo.setRol(existente);
                    nuevo.setSubmodulo(submodulo);
                    nuevo.setPuedeCrear(dto.puedeCrear());
                    nuevo.setPuedeEditar(dto.puedeEditar());
                    nuevo.setPuedeEliminar(dto.puedeEliminar());
                    nuevo.setPuedeLeer(dto.puedeLeer());

                    existente.getPermisos().add(nuevo);
                }
            }

            // 3. Eliminar los permisos que ya no vienen en la petición (si aplica orphanRemoval)
            existente.getPermisos().removeIf(p -> !submodulosEnRequest.contains(p.getSubmodulo().getId()));
        }

        RolEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }
    @Transactional(readOnly = true)
    public RolDTOs.Response obtenerPorId(Long id) {
        RolEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<RolDTOs.Response> obtenerTodos(Boolean activos) {
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
    public List<RolDTOs.Response> buscarPorCriterio(String criterio, Boolean activos) {
        if (criterio == null || criterio.isBlank()) {
            return List.of();
        }
        String c = criterio.trim();
        List<RolEntity> resultados;
        if (activos == null) {
            resultados = repository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(c, c);
        } else {
            // filtrar por estado después de búsqueda por nombre/código
            List<RolEntity> porNombre = repository.findByNombreContainingIgnoreCaseAndEstado(c, activos);
            List<RolEntity> porCodigo = repository.findAll().stream()
                    .filter(r -> r.getCodigo() != null && r.getCodigo().toLowerCase().contains(c.toLowerCase()) && r.isEstado() == activos)
                    .toList();
            // merge sin duplicados
            resultados = new ArrayList<>(porNombre);
            for (RolEntity r : porCodigo) {
                if (resultados.stream().noneMatch(x -> x.getId().equals(r.getId()))) {
                    resultados.add(r);
                }
            }
        }
        return resultados.stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RolDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
        return buscarPorCriterio(nombre, activos);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        RolEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<RolDTOs.ModuloResponse> obtenerModulosJerarquicos() {
        return moduloRepository.findAll().stream()
                .map(mapper::toModuloDTO)
                .toList();
    }

    private void validarNoDuplicado(Long idActual, String nombre) {
        Optional<RolEntity> duplicado = repository.findByNombreIgnoreCase(nombre);
        if (duplicado.isEmpty() || duplicado.get().getId().equals(idActual)) {
            return;
        }
        if (duplicado.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un rol activo con el nombre: " + nombre);
        }
        throw new IllegalArgumentException(
                "Ya existe un rol con el nombre: " + nombre + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo."
        );
    }

    private List<RolPermisoEntity> construirPermisos(List<RolDTOs.PermisoRequest> requests, RolEntity rol) {
        List<RolDTOs.PermisoRequest> permisosUnicos = deduplicarPermisosPorSubmodulo(requests);
        List<RolPermisoEntity> permisos = new ArrayList<>();
        for (RolDTOs.PermisoRequest r : permisosUnicos) {
            SubmoduloEntity sub = submoduloRepository.findById(r.submoduloId())
                    .orElseThrow(() -> new RuntimeException("Submódulo no encontrado con el ID: " + r.submoduloId()));
            RolPermisoEntity e = RolPermisoEntity.builder()
                    .rol(rol)
                    .submodulo(sub)
                    .puedeLeer(Boolean.TRUE.equals(r.puedeLeer()))
                    .puedeCrear(Boolean.TRUE.equals(r.puedeCrear()))
                    .puedeEditar(Boolean.TRUE.equals(r.puedeEditar()))
                    .puedeEliminar(Boolean.TRUE.equals(r.puedeEliminar()))
                    .build();
            permisos.add(e);
        }
        return permisos;
    }

    private List<RolDTOs.PermisoRequest> deduplicarPermisosPorSubmodulo(List<RolDTOs.PermisoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Map<Long, RolDTOs.PermisoRequest> deduplicados = new LinkedHashMap<>();
        for (RolDTOs.PermisoRequest request : requests) {
            if (request == null || request.submoduloId() == null) {
                continue;
            }

            RolDTOs.PermisoRequest actual = deduplicados.get(request.submoduloId());
            if (actual == null) {
                deduplicados.put(request.submoduloId(), request);
                continue;
            }

            deduplicados.put(request.submoduloId(), new RolDTOs.PermisoRequest(
                    request.submoduloId(),
                    Boolean.TRUE.equals(actual.puedeLeer()) || Boolean.TRUE.equals(request.puedeLeer()),
                    Boolean.TRUE.equals(actual.puedeCrear()) || Boolean.TRUE.equals(request.puedeCrear()),
                    Boolean.TRUE.equals(actual.puedeEditar()) || Boolean.TRUE.equals(request.puedeEditar()),
                    Boolean.TRUE.equals(actual.puedeEliminar()) || Boolean.TRUE.equals(request.puedeEliminar())
            ));
        }
        return new ArrayList<>(deduplicados.values());
    }

    private String generarCodigoRol() {
        long total = repository.count();
        return String.format("ROL-%02d", total + 1);
    }
}
