package com.example.demo.modules.usuarios.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository repository;
    private final RolMapper mapper;
    private final ModuloRepository moduloRepository;

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

    @Transactional(readOnly = true)
    public List<RolDTOs.ModuloResponse> obtenerModulosJerarquicos() {
        return moduloRepository.findAll().stream()
                .map(mapper::toModuloDTO)
                .toList();
    }

}
