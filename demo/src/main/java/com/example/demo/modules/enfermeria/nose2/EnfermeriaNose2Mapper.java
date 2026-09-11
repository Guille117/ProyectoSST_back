package com.example.demo.modules.enfermeria.nose2;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnfermeriaNose2Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    EnfermeriaNose2Entity toEntity(EnfermeriaNose2DTOs.Request request);

    EnfermeriaNose2DTOs.Response toDTO(EnfermeriaNose2Entity entity);
}
