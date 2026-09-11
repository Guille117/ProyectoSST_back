package com.example.demo.modules.medicina.nose1;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicinaNose1Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    MedicinaNose1Entity toEntity(MedicinaNose1DTOs.Request request);

    MedicinaNose1DTOs.Response toDTO(MedicinaNose1Entity entity);
}
