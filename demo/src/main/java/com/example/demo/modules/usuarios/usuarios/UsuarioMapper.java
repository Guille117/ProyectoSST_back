package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.puestoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "puesto", ignore = true)
    @Mapping(target = "horario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    UsuarioEntity toEntity(UsuarioDTOs.Request request);

    // Para update: no se mapea automáticamente
    default UsuarioDTOs.Response toDTO(UsuarioEntity entity) {
        if (entity == null) return null;
        PersonaEntity p = entity.getPersona();
        puestoEntity puesto = entity.getPuesto();
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
                puesto != null ? puesto.getNombre() : null,
                entity.getHorario() != null ? entity.getHorario().getId() : null,
                entity.getHorario() != null ? entity.getHorario().getCodigo() : null,
                entity.getHorario() != null ? entity.getHorario().getNombre() : null,
                entity.getRoles() == null ? List.of() : entity.getRoles().stream()
                    .map(rol -> new UsuarioDTOs.RolResponse(rol.getId(), rol.getCodigo(), rol.getNombre()))
                    .toList(),
                entity.getUsername(),
                entity.isEstado()
        );
    }
}
