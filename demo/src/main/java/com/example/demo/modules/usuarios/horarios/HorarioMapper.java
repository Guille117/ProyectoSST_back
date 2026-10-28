package com.example.demo.modules.usuarios.horarios;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HorarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    @Mapping(target = "esRotativo", expression = "java(request.esRotativo() != null ? request.esRotativo() : false)")
    @Mapping(target = "semanalDetalles", ignore = true)
    @Mapping(target = "turnoDetalle", ignore = true)
    HorarioEntity toEntity(HorarioDTOs.Request request);

    HorarioDTOs.Response toDTO(HorarioEntity entity);

    default HorarioDTOs.DetalleSemanalResponse toDetalleSemanalDTO(HorarioSemanalDetalleEntity entity) {
        if (entity == null) return null;
        return new HorarioDTOs.DetalleSemanalResponse(
                entity.getId(),
                entity.getDiaSemana(),
                entity.getHoraEntrada(),
                entity.getHoraSalida(),
                entity.isActivo()
        );
    }

    HorarioDTOs.TurnoDetalleResponse toTurnoDTO(HorarioTurnoDetalleEntity entity);
}
