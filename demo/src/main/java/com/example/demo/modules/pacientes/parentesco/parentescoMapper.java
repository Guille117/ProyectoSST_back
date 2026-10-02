package com.example.demo.modules.pacientes.parentesco;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface parentescoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    parentescoEntity toEntity(parentescoDTOs.Request request);

    parentescoDTOs.Response toDTO(parentescoEntity entity);
}