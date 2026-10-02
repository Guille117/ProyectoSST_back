package com.example.demo.modules.pacientes.parentesco;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.utils.StringNormalizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class parentescoService extends serviceBase<parentescoEntity> {

    private final parentescoRepository repository;
    private final parentescoMapper mapper;

    @Override
    protected repositoryBase<parentescoEntity> getRepository() {
        return repository;
    }

    @Transactional
    public parentescoDTOs.Response crear(parentescoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        validarNombre(nombre);

        parentescoEntity entity = mapper.toEntity(request);
        entity.setNombre(nombre);
        entity.setEstado(request.estado() != null ? request.estado() : true);
        return mapper.toDTO(super.guardar(entity));
    }

    @Transactional(readOnly = true)
    public List<parentescoDTOs.Response> obtenerTodos(Boolean activos) {
        List<parentescoEntity> entidades = activos == null
                ? repository.findAll()
                : repository.findByEstado(activos);
        return entidades.stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return repository.countByEstado(true);
    }

    @Transactional(readOnly = true)
    public List<parentescoDTOs.Response> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return List.of();
        }

        return repository.findByNombreContainingIgnoreCase(StringNormalizer.normalizarTexto(nombre))
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public parentescoDTOs.Response obtenerPorId(Long id) {
        parentescoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parentesco no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional
    public parentescoDTOs.Response actualizar(Long id, parentescoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        validarNombre(nombre);
        return super.actualizar(id, nombre, existente -> {
                    if (request.estado() != null) {
                        existente.setEstado(request.estado());
                    }
                })
                .map(mapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Parentesco no encontrado con el ID: " + id));
    }

    @Transactional
    public java.util.Optional<parentescoDTOs.Response> alternarEstado(Long id) {
        return super.cambiarEstado(id).map(mapper::toDTO);
    }

    private void validarNombre(String nombre) {
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

}