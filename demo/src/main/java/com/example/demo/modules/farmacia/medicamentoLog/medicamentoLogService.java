package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaRepository;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminRepository;
import com.example.demo.utils.StringNormalizer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    @PersistenceContext 
    private EntityManager entityManager; 

    public Map<String, Long> obtenerConteoCatalogosFarmacia(){
        Map<String, Long> conteo = new HashMap<>();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("catalogosFarmacia");
        List<Object[]> resultados = query.getResultList();

        for(Object[] fila: resultados){
            String nombreTabla = (String) fila[0];
            Long registros = ((Number) fila[1]).longValue();  
            conteo.put(nombreTabla, registros);
        }
        return conteo;
    }

    @Transactional
    public medicamentoLogDTOs.Response crear(medicamentoLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("La solicitud es obligatoria");
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        String dosis = StringNormalizer.normalizarTexto(request.dosis());
        validarNombreUnico(null, nombre);
        medicamentoLogEntity entity = construir(request, nombre, dosis);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public medicamentoLogDTOs.Response actualizar(Long id, medicamentoLogDTOs.Request request) {
        if (request == null) throw new IllegalArgumentException("Sin datos para actualizar");
        medicamentoLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
        String nombre = StringNormalizer.normalizarTexto(request.nombre());
        String dosis = StringNormalizer.normalizarTexto(request.dosis());
        validarNombreUnico(id, nombre);
        entity.setNombre(nombre);
        entity.setDosis(dosis);
        asignarRelaciones(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public medicamentoLogDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<medicamentoLogDTOs.Response> obtenerTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<medicamentoLogDTOs.Response> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return List.of();
        return repository.findByNombreContainingIgnoreCase(nombre.trim()).stream().map(this::toResponse).toList();
    }

    private medicamentoLogEntity construir(medicamentoLogDTOs.Request request, String nombre, String dosis) {
        medicamentoLogEntity entity = medicamentoLogEntity.builder().nombre(nombre).dosis(dosis).build();
        asignarRelaciones(entity, request);
        return entity;
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

    private void validarNombreUnico(Long idActual, String nombre) {
        repository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un medicamento con el nombre: " + nombre);
            }
        });
    }

    private medicamentoLogDTOs.Response toResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.Response(entity.getId(), entity.getNombre(), entity.getDosis(),
                entity.getUnidadMedida().getId(), entity.getUnidadMedida().getNombre(), entity.getMarca().getId(),
                entity.getMarca().getNombre(), entity.getViaAdmin().getId(), entity.getViaAdmin().getNombre(),
                entity.getPresentacion().getId(), entity.getPresentacion().getNombre());
    }
}