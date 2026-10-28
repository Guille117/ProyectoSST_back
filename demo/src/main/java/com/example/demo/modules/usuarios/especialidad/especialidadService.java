package com.example.demo.modules.usuarios.especialidad;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class especialidadService extends serviceBase<especialidadEntity> {

    private final especialidadRepository especialidadRepository;
    private final UsuarioRepository usuarioRepository;

    public especialidadService(especialidadRepository especialidadRepository, UsuarioRepository usuarioRepository) {
        this.especialidadRepository = especialidadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected repositoryBase<especialidadEntity> getRepository() {
        return especialidadRepository;
    }

    @Override
    protected boolean estaRelacionado(especialidadEntity entidad) {
        return usuarioRepository.existsByEspecialidad_Id(entidad.getId());
    }

    @Transactional
    public long contarEspecialidades() {
        return especialidadRepository.countByEstado(true);
    }
}