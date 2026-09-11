package com.example.demo.modules.usuarios.usuarios;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "puesto", ignore = true)
    @Mapping(target = "horario", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "password", ignore = true)
    UsuarioEntity toEntity(UsuarioDTOs.Request request);

    // Para update: no se mapea automáticamente
    default UsuarioDTOs.Response toDTO(UsuarioEntity entity) {
        if (entity == null) return null;
        PersonaEntity p = entity.getPersona();
        PuestoEntity puesto = entity.getPuesto();
        return new UsuarioDTOs.Response(
                entity.getId(),
                entity.getCodigo(),
                p != null ? p.getCui() : null,
                p != null ? p.getNombres() : null,
                p != null ? p.getApellidos() : null,
                p != null ? p.getSexo() : null,
                p != null ? p.getFechaNacimiento() : null,
                p != null ? p.getTelefono() : null,
                p != null ? p.getEmail() : null,
                puesto != null ? puesto.getId() : null,
                puesto != null ? puesto.getCodigo() : null,
                puesto != null ? puesto.getNombre() : null,
                entity.getHorario() != null ? entity.getHorario().getId() : null,
                entity.getHorario() != null ? entity.getHorario().getCodigo() : null,
                entity.getHorario() != null ? entity.getHorario().getNombre() : null,
                entity.getRol() != null ? entity.getRol().getId() : null,
                entity.getRol() != null ? entity.getRol().getCodigo() : null,
                entity.getRol() != null ? entity.getRol().getNombre() : null,
                entity.getUsername(),
                entity.isEstado()
        );
    }
}
