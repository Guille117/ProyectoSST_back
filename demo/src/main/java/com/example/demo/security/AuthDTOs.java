package com.example.demo.security;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank(message = "El username es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}

    public record AuthResponse(
            String token,
            String tipo,
            Long id,
            String codigo,
            String username,
            String nombreCompleto,
            String puesto,
            String rol,
            boolean estado,
            List<PermisoDTO> permisos
    ) {}

    public record PermisoDTO(
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
}
