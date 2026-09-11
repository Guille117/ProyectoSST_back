package com.example.demo.modules.paciente.pacientes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    PacienteEntity toEntity(PacienteDTOs.Request request);

    PacienteDTOs.Response toDTO(PacienteEntity entity);
}
