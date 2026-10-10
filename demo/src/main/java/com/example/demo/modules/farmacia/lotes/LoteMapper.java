package com.example.demo.modules.farmacia.lotes;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "insumoLog", ignore = true)
    @Mapping(target = "medicamentoLog", ignore = true)
    @Mapping(target = "compra", ignore = true)
    LoteEntity toEntity(LoteDTOs.Request request);

    @Mapping(target = "idInsumoLog", source = "insumoLog.id")
    @Mapping(target = "nombreInsumo", source = "insumoLog.nombre")
    @Mapping(target = "idMedicamentoLog", source = "medicamentoLog.id")
    @Mapping(target = "nombreMedicamento", source = "medicamentoLog.nombre")
    @Mapping(target = "idCompra", source = "compra.id")
    @Mapping(target = "codigoCompra", source = "compra.codigo")
    LoteDTOs.Response toDTO(LoteEntity entity);
}
