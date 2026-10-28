package com.example.demo.modules.usuarios.roles;

import java.util.List;

public class RolDTOs {

    public record Response(
            Long id,
            String codigo,
            String nombre,
            boolean estado,
            List<PermisoResponse> permisos
    ) {}

    public record PermisoResponse(
            Long id,
            Long submoduloId,
            String submoduloCodigo,
            String submoduloNombre,
            String moduloCodigo,
            String moduloNombre,
            boolean puedeLeer,
            boolean puedeCrear,
            boolean puedeEditar,
            boolean puedeEliminar
    ) {}

    public record ModuloResponse(
            Long id,
            String codigo,
            String nombre,
            boolean estado,
            List<SubmoduloResponse> submodulos
    ) {}

    public record SubmoduloResponse(
            Long id,
            String codigo,
            String nombre,
            boolean estado
    ) {}
}
