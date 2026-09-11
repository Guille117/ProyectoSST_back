package com.example.demo.modules.farmacia.compras;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    CompraEntity toEntity(CompraDTOs.Request request);

    CompraDTOs.Response toDTO(CompraEntity entity);
}
