package com.example.demo.modules.farmacia.motivoBaja;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class motivoBajaService extends serviceBase<motivoBajaEntity> {
    private final motivoBajaRepository motivoBajaRepository;

    public motivoBajaService(motivoBajaRepository motivoBajaRepository) {
        this.motivoBajaRepository = motivoBajaRepository;
    }

    @Override
    protected repositoryBase<motivoBajaEntity> getRepository() {
        return motivoBajaRepository;
    }

    public Optional<motivoBajaEntity> actualizar(Long id, motivoBajaEntity detalles) {
        return actualizar(id, detalles.getNombre(), entidadActual ->
                entidadActual.setDescripcion(detalles.getDescripcion()));
    }
}
