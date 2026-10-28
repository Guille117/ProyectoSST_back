package com.example.demo.modules.usuarios.roles;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RolMapper {

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
