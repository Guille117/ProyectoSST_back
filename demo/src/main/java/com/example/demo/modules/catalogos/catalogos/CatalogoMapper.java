package com.example.demo.modules.catalogos.catalogos;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    CatalogoEntity toEntity(CatalogoDTOs.Request request);

    CatalogoDTOs.Response toDTO(CatalogoEntity entity);
}
