package com.example.demo.modules.usuarios.configuracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConfiguracionDTOs {

    public record CambioUsuarioRequest(
            @NotBlank(message = "El username es obligatorio")
            @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
            String username
    ) {
    }

    public record CambioUsuarioResponse(
            String token,
            String username
    ) {
    }

    public record CambioPasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria")
            String passwordActual,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
            String passwordNueva,
            @NotBlank(message = "La confirmación de contraseña es obligatoria")
            String confirmarPassword
    ) {
    }
}
