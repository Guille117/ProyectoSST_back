package com.example.demo.modules.farmacia.proveedores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    ProveedorEntity toEntity(ProveedorDTOs.Request request);

    ProveedorDTOs.Response toDTO(ProveedorEntity entity);
}
