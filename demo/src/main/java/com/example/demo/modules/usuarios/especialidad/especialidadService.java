package com.example.demo.modules.usuarios.especialidad;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class especialidadService extends serviceBase<especialidadEntity> {

    private final especialidadRepository especialidadRepository;

    public especialidadService(especialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    protected repositoryBase<especialidadEntity> getRepository() {
        return especialidadRepository;
    }

    @Transactional
    public long contarEspecialidades() {
        return especialidadRepository.countByEstado(true);
    }
}