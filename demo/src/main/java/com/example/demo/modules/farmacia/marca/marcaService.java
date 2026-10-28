package com.example.demo.modules.farmacia.marca;

import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import org.springframework.stereotype.Service;

@Service
public class marcaService extends serviceBase<marcaEntity> {
    private final marcaRepository marcaRepository;
    private final medicamentoLogRepository medicamentoLogRepository;
    private final insumosLogRepository insumosLogRepository;

    public marcaService(marcaRepository marcaRepository,
                        medicamentoLogRepository medicamentoLogRepository,
                        insumosLogRepository insumosLogRepository) {
        this.marcaRepository = marcaRepository;
        this.medicamentoLogRepository = medicamentoLogRepository;
        this.insumosLogRepository = insumosLogRepository;
    }

    @Override
    protected repositoryBase<marcaEntity> getRepository() {
        return marcaRepository;
    }

    @Override
    protected boolean estaRelacionado(marcaEntity entidad) {
        return medicamentoLogRepository.existsByMarca_Id(entidad.getId())
                || insumosLogRepository.existsByMarca_Id(entidad.getId());
    }
}