package com.example.demo.modules.usuarios.roles;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RolDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,

            Boolean estado,

            @Valid List<PermisoRequest> permisos
    ) {}

    public record PermisoRequest(
            @NotNull(message = "El submoduloId es obligatorio") Long submoduloId,

            Boolean puedeLeer,
            Boolean puedeCrear,
            Boolean puedeEditar,
            Boolean puedeEliminar
    ) {}

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
