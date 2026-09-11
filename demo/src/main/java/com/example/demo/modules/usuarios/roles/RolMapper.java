package com.example.demo.modules.usuarios.roles;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RolMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    @Mapping(target = "permisos", ignore = true)
    RolEntity toEntity(RolDTOs.Request request);

    RolDTOs.Response toDTO(RolEntity entity);

    default RolDTOs.PermisoResponse toPermisoDTO(RolPermisoEntity entity) {
        if (entity == null) return null;
        SubmoduloEntity sub = entity.getSubmodulo();
        ModuloEntity mod = sub != null ? sub.getModulo() : null;
        return new RolDTOs.PermisoResponse(
                entity.getId(),
                sub != null ? sub.getId() : null,
                sub != null ? sub.getCodigo() : null,
                sub != null ? sub.getNombre() : null,
                mod != null ? mod.getCodigo() : null,
                mod != null ? mod.getNombre() : null,
                entity.isPuedeLeer(),
                entity.isPuedeCrear(),
                entity.isPuedeEditar(),
                entity.isPuedeEliminar()
        );
    }

    default RolDTOs.ModuloResponse toModuloDTO(ModuloEntity entity) {
        if (entity == null) return null;
        return new RolDTOs.ModuloResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.isEstado(),
                entity.getSubmodulos() == null ? List.of() :
                        entity.getSubmodulos().stream().map(this::toSubmoduloDTO).toList()
        );
    }

    default RolDTOs.SubmoduloResponse toSubmoduloDTO(SubmoduloEntity entity) {
        if (entity == null) return null;
        return new RolDTOs.SubmoduloResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.isEstado()
        );
    }
}
