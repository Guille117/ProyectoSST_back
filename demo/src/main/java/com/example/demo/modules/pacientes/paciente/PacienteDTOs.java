package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class PacienteDTOs {

    public record PersonaRequest(
            @NotBlank @Pattern(regexp = "[0-9]{13}") String cui,
            @NotBlank String nombres,
            @NotBlank String apellidos,
            @NotNull @PastOrPresent LocalDate fechaNacimiento,
            @NotNull Sexo sexo,
            @Pattern(regexp = "^(|[0-9]{8,15})$") String telefono,
            @Email String email
    ) {}

    public record PacienteRequest(
            @NotNull @Valid PersonaRequest persona,
            EstadoCivil estadoCivil,
            @Size(max = 255) String direccion,
            @Size(max = 100) String ocupacion
    ) {}

    public record ResponsableRequest(
            @NotNull @Valid PersonaRequest persona,
            @NotBlank @Size(max = 50) String parentesco,
            @Size(max = 255) String direccion
    ) {}

    public record ReferenciaRequest(
            @NotBlank @Size(max = 150) String nombreInstitucion,
            @NotBlank @Size(max = 1000) String motivoReferencia,
            @Size(max = 2048) String urlDocumento
    ) {}

    public record EpisodioRequest(
            @NotNull TipoAtencion tipoAtencion,
            @NotBlank @Size(max = 2000) String descripcion,
            @NotNull @Positive Long medicoId,
            @Valid ResponsableRequest responsable,
            @Valid ReferenciaRequest referencia
    ) {}

    public record Request(
            @NotNull @Valid PacienteRequest paciente,
            @NotNull @Valid EpisodioRequest episodio
    ) {}

    public record Response(
            Long pacienteId,
            Long expedienteId,
            Long episodioId,
            Long responsableId,
            Long referenciaId,
            boolean pacienteExistente
    ) {}
}