package com.example.demo.modules.reportes.reportes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReporteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    ReporteEntity toEntity(ReporteDTOs.Request request);

    ReporteDTOs.Response toDTO(ReporteEntity entity);
}
