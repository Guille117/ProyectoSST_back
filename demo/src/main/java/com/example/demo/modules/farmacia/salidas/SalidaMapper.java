package com.example.demo.modules.farmacia.salidas;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SalidaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    SalidaEntity toEntity(SalidaDTOs.Request request);

    SalidaDTOs.Response toDTO(SalidaEntity entity);
}
