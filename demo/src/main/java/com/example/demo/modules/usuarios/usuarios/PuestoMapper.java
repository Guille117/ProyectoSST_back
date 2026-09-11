package com.example.demo.modules.usuarios.usuarios;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PuestoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    PuestoEntity toEntity(PuestoDTOs.Request request);

    PuestoDTOs.Response toDTO(PuestoEntity entity);
}
