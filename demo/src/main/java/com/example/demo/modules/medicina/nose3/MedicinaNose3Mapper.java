package com.example.demo.modules.medicina.nose3;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicinaNose3Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    MedicinaNose3Entity toEntity(MedicinaNose3DTOs.Request request);

    MedicinaNose3DTOs.Response toDTO(MedicinaNose3Entity entity);
}
