package com.example.demo.modules.usuarios.usuarios;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public class UsuarioDTOs {

    public record Request(
                        @Valid @NotNull(message = "Los datos personales son obligatorios") PersonaRequest persona,

                        @NotNull(message = "El puesto es obligatorio") Long puestoId,

                        @NotNull(message = "El horario es obligatorio") Long horarioId,

                        @NotEmpty(message = "Debe seleccionar al menos un rol") List<Long> rolIds,

                        @NotBlank(message = "El username es obligatorio")
                        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
                        String username,

                        @JsonAlias({"especialidad", "especialidad_id"}) Long especialidadId,

                        Boolean estado
        ) {
                public Request(PersonaRequest persona, Long puestoId, Long horarioId, List<Long> rolIds,
                               String username, Boolean estado) {
                        this(persona, puestoId, horarioId, rolIds, username, null, estado);
                }
        }

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

            @NotEmpty(message = "Debe seleccionar al menos un rol") List<Long> rolIds,

            @NotBlank(message = "El username es obligatorio")
            @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
            String username,

            // password opcional en actualización
            String password,

            String confirmPassword,

            @JsonAlias({"especialidad", "especialidad_id"}) Long especialidadId,

            Boolean estado
    ) {
            public UpdateRequest(PersonaRequest persona, Long puestoId, Long horarioId, List<Long> rolIds,
                                 String username, String password, String confirmPassword, Boolean estado) {
                    this(persona, puestoId, horarioId, rolIds, username, password, confirmPassword, null, estado);
            }
    }

    public record Response(
            Long id,
            String codigo,
                        String username,
            String nombres,
            String apellidos,
                        String cui,
            Sexo sexo,
            LocalDate fechaNacimiento,
            String telefono,
            String email,
                        PuestoResponse puesto,
                        HorarioResponse horario,
                        List<Long> rolIds,
                        List<String> roles,
                        boolean estado,
                        EspecialidadResponse especialidad
    ) {
                // Compatibilidad interna con los constructores usados por código existente.
        public Response(
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
                List<RolResponse> roles,
                String username,
                boolean estado
        ) {
                        this(id, codigo, username, nombres, apellidos, cui, sexo, fechaNacimiento, telefono, email,
                                        new PuestoResponse(puestoId, puestoNombre),
                                        new HorarioResponse(horarioId, horarioNombre),
                                        roles == null ? List.of() : roles.stream().map(RolResponse::id).toList(),
                                        roles == null ? List.of() : roles.stream().map(RolResponse::nombre).toList(),
                                        estado, null);
        }
    }

    public record RolResponse(
            Long id,
            String codigo,
            String nombre
    ) {}

        public record EspecialidadResponse(Long id, String nombre) {}

        public record PuestoResponse(Long id, String nombre) {}

        public record HorarioResponse(Long id, String nombre) {}

    public record ListResponse(
            Long id,
            String codigo,
            String nombreCompleto,
            String telefono,
            List<String> roles,
            boolean estado,
            EspecialidadResponse especialidad
    ) {
            public ListResponse(Long id, String codigo, String nombreCompleto, String telefono,
                                List<String> roles, boolean estado) {
                    this(id, codigo, nombreCompleto, telefono, roles, estado, null);
            }
    }

        public record MedicoResponse(Long id, String nombreCompleto, String especialidad) {
                public MedicoResponse(Long id, String nombreCompleto) {
                        this(id, nombreCompleto, null);
                }
        }
}
