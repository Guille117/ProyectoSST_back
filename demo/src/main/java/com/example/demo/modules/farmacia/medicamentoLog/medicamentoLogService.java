package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaRepository;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminRepository;
import com.example.demo.utils.StringNormalizer;

import java.math.BigDecimal;
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
        medicamentoLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private medicamentoLogEntity construir(medicamentoLogDTOs.Request request, String nombre, BigDecimal dosis) {
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

    private medicamentoLogDTOs.Response toResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.Response(entity.getId(), entity.getNombre(), entity.getDosis(),
                entity.getUnidadMedida().getId(), entity.getUnidadMedida().getNombre(), entity.getMarca().getId(),
                entity.getMarca().getNombre(), entity.getViaAdmin().getId(), entity.getViaAdmin().getNombre(),
                entity.getPresentacion().getId(), entity.getPresentacion().getNombre());
    }

    private medicamentoLogDTOs.MedicamentoLogResponse toListadoResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.MedicamentoLogResponse(entity.getId(), entity.getNombre(), entity.getDosis(),
                entity.getMarca().getNombre(), entity.getPresentacion().getNombre(),
            entity.getViaAdmin().getNombre(), entity.getUnidadMedida().getAbreviatura(), entity.isEstado());
    }

    private medicamentoLogDTOs.MedicamentoLogBusquedaResponse toBusquedaResponse(medicamentoLogEntity entity) {
        return new medicamentoLogDTOs.MedicamentoLogBusquedaResponse(entity.getNombre(), entity.getDosis(),
                entity.getUnidadMedida().getId(), entity.getViaAdmin().getId(),
                entity.getPresentacion().getId(), entity.getMarca().getId());
    }

}