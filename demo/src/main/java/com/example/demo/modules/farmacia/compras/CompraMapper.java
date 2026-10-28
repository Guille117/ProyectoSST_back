package com.example.demo.modules.farmacia.compras;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "comprobante", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    CompraEntity toEntity(CompraDTOs.Request request);

    @Mapping(target = "proveedorId", source = "proveedor.id")
    @Mapping(target = "proveedorNombre", source = "proveedor.nombre")
    @Mapping(target = "lotes", ignore = true)
    CompraDTOs.Response toDTO(CompraEntity entity);
}
