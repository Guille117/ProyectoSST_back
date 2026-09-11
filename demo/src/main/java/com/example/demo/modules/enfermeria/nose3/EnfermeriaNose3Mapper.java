package com.example.demo.modules.enfermeria.nose3;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnfermeriaNose3Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    EnfermeriaNose3Entity toEntity(EnfermeriaNose3DTOs.Request request);

    EnfermeriaNose3DTOs.Response toDTO(EnfermeriaNose3Entity entity);
}
