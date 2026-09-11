package com.example.demo.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank(message = "El username es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}

    // Primer ingreso o restablecimiento de credenciales: requiere el PIN de un solo uso
    // generado al crear el usuario (ver UsuarioService.crear).
    public record EstablecerCredencialesRequest(
            @NotBlank(message = "El username es obligatorio") String username,

            @NotBlank(message = "El PIN es obligatorio")
            @Pattern(regexp = "^[0-9]{6}$", message = "El PIN debe tener 6 dígitos")
            String pin,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
            String password,

            @NotBlank(message = "La confirmación de contraseña es obligatoria")
            String confirmPassword
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
