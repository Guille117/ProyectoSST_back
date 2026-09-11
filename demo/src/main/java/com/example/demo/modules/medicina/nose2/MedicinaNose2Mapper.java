package com.example.demo.modules.medicina.nose2;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicinaNose2Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    MedicinaNose2Entity toEntity(MedicinaNose2DTOs.Request request);

    MedicinaNose2DTOs.Response toDTO(MedicinaNose2Entity entity);
}
