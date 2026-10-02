package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class unidadMedidaService extends serviceBase<unidadMedidaEntity> {
    private final unidadMedidaRepository unidadMedidaRepository;
    private final medicamentoLogRepository medicamentoLogRepository;

    public unidadMedidaService(unidadMedidaRepository unidadMedidaRepository,
                               medicamentoLogRepository medicamentoLogRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.medicamentoLogRepository = medicamentoLogRepository;
    }

    @Override
    protected repositoryBase<unidadMedidaEntity> getRepository() {
        return unidadMedidaRepository;
    }

    @Override
    protected boolean estaRelacionado(unidadMedidaEntity entidad) {
        return medicamentoLogRepository.existsByUnidadMedida_Id(entidad.getId());
    }

    public Optional<unidadMedidaEntity> actualizar(Long id, unidadMedidaEntity detalles) {
        return actualizar(id, detalles.getNombre(), entidadActual ->
                entidadActual.setAbreviatura(detalles.getAbreviatura()));
    }
}