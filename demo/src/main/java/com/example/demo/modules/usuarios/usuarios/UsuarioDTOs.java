package com.example.demo.modules.usuarios.usuarios;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.time.LocalDate;

public class UsuarioDTOs {

    public record Request(
                        @Valid @NotNull(message = "Los datos personales son obligatorios") PersonaRequest persona,

                        @NotNull(message = "El puesto es obligatorio") Long puestoId,

                        @NotNull(message = "El horario es obligatorio") Long horarioId,

                        @NotNull(message = "El rol es obligatorio") Long rolId,

                        @NotBlank(message = "El username es obligatorio")
                        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
                        String username,

                        Boolean estado
        ) {}

        // Respuesta de creación: incluye el PIN de un solo uso que el usuario debe usar
        // junto con /api/v1/auth/establecer-credenciales para definir su contraseña.
        public record CrearResponse(
                Response usuario,
                String pin,
                java.time.LocalDateTime pinExpiracion
        ) {}

        public record PersonaRequest(
            @NotBlank(message = "El CUI es obligatorio")
            @Pattern(regexp = "^[0-9]{13}$", message = "El CUI debe tener exactamente 13 dígitos numéricos")
            String cui,

            @NotBlank(message = "Los nombres son obligatorios") String nombres,

            @NotBlank(message = "Los apellidos son obligatorios") String apellidos,

            @NotNull(message = "El sexo es obligatorio") Sexo sexo,

            @NotNull(message = "La fecha de nacimiento es obligatoria") LocalDate fechaNacimiento,

            @Pattern(regexp = "^(|[0-9]{8,15})$", message = "El teléfono debe tener entre 8 y 15 dígitos") String telefono,

            @Email(message = "Formato de email inválido") String email
    ) {}

    // Para actualización: password opcional
    public record UpdateRequest(
            @Valid @NotNull(message = "Los datos personales son obligatorios") PersonaRequest persona,

            @NotNull(message = "El puesto es obligatorio") Long puestoId,

            @NotNull(message = "El horario es obligatorio") Long horarioId,

            @NotNull(message = "El rol es obligatorio") Long rolId,

            @NotBlank(message = "El username es obligatorio")
            @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
            String username,

            // password opcional en actualización
            String password,

            String confirmPassword,

            Boolean estado
    ) {}

    public record Response(
            Long id,
            String codigo,
            String cui,
            String nombres,
            String apellidos,
            Sexo sexo,
            LocalDate fechaNacimiento,
            String telefono,
            String email,
            Long puestoId,
            String puestoNombre,
            Long horarioId,
            String horarioCodigo,
            String horarioNombre,
            Long rolId,
            String rolCodigo,
            String rolNombre,
            String username,
            boolean estado
    ) {}
}
