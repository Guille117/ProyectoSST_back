package com.example.demo.modules.reportes.bitacora;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BitacoraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    BitacoraEntity toEntity(BitacoraDTOs.Request request);

    BitacoraDTOs.Response toDTO(BitacoraEntity entity);
}
