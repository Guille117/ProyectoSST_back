package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PacienteDTOs {

    public record PersonaRequest(
            @Pattern(regexp = "(|[0-9]{13})", message = "El CUI debe tener exactamente 13 dígitos") String cui,
            @NotBlank String nombres,
            @NotBlank String apellidos,
            @NotNull @PastOrPresent LocalDate fechaNacimiento,
            @NotNull Sexo sexo,
            @Pattern(regexp = "^(|[0-9]{8,15})$") String telefono,
            @Email String email
    ) {}

    public record PacienteRequest(
            @Positive Long pacienteId,
            @NotNull @Valid PersonaRequest persona,
            EstadoCivil estadoCivil,
            @Size(max = 255) String direccion,
            @Size(max = 100) String ocupacion,
            @Positive Long camaId,
            List<@Positive Long> medicoIds,
            List<@Positive Long> alergiaIds,
            List<@Positive Long> enfermedadIds
    ) {
        /** Constructor de compatibilidad para solicitudes anteriores sin lista de médicos. */
        public PacienteRequest(
                Long pacienteId,
                PersonaRequest persona,
                EstadoCivil estadoCivil,
                String direccion,
                String ocupacion,
                Long camaId,
                List<Long> alergiaIds,
                List<Long> enfermedadIds) {
            this(pacienteId, persona, estadoCivil, direccion, ocupacion, camaId, null,
                    alergiaIds, enfermedadIds);
        }
    }

    public record ResponsableRequest(
            @NotNull @Valid PersonaRequest persona,
            @NotNull @Positive Long parentescoId,
            @Size(max = 255) String direccion
    ) {}

    public record ReferenciaRequest(
            @NotNull @Positive Long institucionId,
            @NotBlank @Size(max = 1000) String motivoReferencia
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
            @NotNull @Valid EpisodioRequest episodio,
            // Usuario que registra la atención y sube los documentos. Si no llega, se usa el médico.
            @Positive Long usuarioId
    ) {}

    public record Response(
            Long pacienteId,
            Long expedienteId,
            Long episodioId,
            LocalDateTime fechaHoraIngreso,
            Long responsableId,
            Long referenciaId,
            boolean pacienteExistente
    ) {}

    public record ListadoResponse(
            Long pacienteId,
            String codigoExpediente,
            Long episodioId,
            Long ultimoEpisodioId,
            LocalDateTime fechaHoraIngreso,
            String nombreCompleto,
            String cui,
            Sexo sexo,
            Integer edad,
            String telefono,
            TipoAtencion tipoTratamiento,
            boolean estado,
            String estadoClinicoNombre
    ) {}

    /** Datos del responsable del episodio, completos para reutilizarlos en la edición. */
    public record ResponsableDetalle(
            Long id,
            String cui,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            Sexo sexo,
            String telefono,
            Long parentescoId,
            String parentescoNombre,
            String direccion
    ) {}

    /** Datos de la referencia del episodio, completos para reutilizarlos en la edición. */
    public record ReferenciaDetalle(
            Long id,
            Long institucionId,
            String institucionNombre,
            String motivoReferencia
    ) {}

    /** Elemento de un catálogo clínico asociado al paciente. */
    public record CatalogoDetalle(Long id, String nombre) {}

    /** Médico asignado al paciente, con la especialidad para mostrarla en la ficha. */
    public record MedicoDetalle(Long id, String nombreCompleto, String especialidad) {}

    /** Detalle del paciente con su episodio activo y los datos necesarios para editarlo. */
    public record DetalleResponse(
            Long pacienteId,
            String codigoExpediente,
            String nombreCompleto,
            String nombres,
            String apellidos,
            String cui,
            LocalDate fechaNacimiento,
            Integer edad,
            Sexo sexo,
            String telefono,
            String email,
            EstadoCivil estadoCivil,
            String direccion,
            String ocupacion,
            boolean estado,
            Long episodioId,
            TipoAtencion tipoAtencion,
            String descripcion,
            LocalDateTime fechaHoraIngreso,
            Long medicoId,
            String medicoNombre,
            List<MedicoDetalle> medicos,
            Long camaId,
            String camaCodigo,
            ResponsableDetalle responsable,
            ReferenciaDetalle referencia,
            List<CatalogoDetalle> alergias,
            List<CatalogoDetalle> enfermedades,
            String estadoClinicoNombre
    ) {
        /** Constructor de compatibilidad para consumidores anteriores a la lista de médicos. */
        public DetalleResponse(
                Long pacienteId,
                String codigoExpediente,
                String nombreCompleto,
                String nombres,
                String apellidos,
                String cui,
                LocalDate fechaNacimiento,
                Integer edad,
                Sexo sexo,
                String telefono,
                String email,
                EstadoCivil estadoCivil,
                String direccion,
                String ocupacion,
                boolean estado,
                Long episodioId,
                TipoAtencion tipoAtencion,
                String descripcion,
                LocalDateTime fechaHoraIngreso,
                Long medicoId,
                String medicoNombre,
                Long camaId,
                String camaCodigo,
                ResponsableDetalle responsable,
                ReferenciaDetalle referencia,
                List<CatalogoDetalle> alergias,
                List<CatalogoDetalle> enfermedades,
                String estadoClinicoNombre) {
            this(pacienteId, codigoExpediente, nombreCompleto, nombres, apellidos, cui,
                    fechaNacimiento, edad, sexo, telefono, email, estadoCivil, direccion,
                    ocupacion, estado, episodioId, tipoAtencion, descripcion, fechaHoraIngreso,
                    medicoId, medicoNombre, List.of(), camaId, camaCodigo, responsable,
                    referencia, alergias, enfermedades, estadoClinicoNombre);
        }
    }

    /** Actualización de un paciente; el episodio es opcional para pacientes sin atención activa. */
    public record ActualizacionRequest(
            @NotNull @Valid PacienteRequest paciente,
            @Valid EpisodioRequest episodio,
            // Usuario que registra la edición y sube el documento de referencia.
            @Positive Long usuarioId
    ) {}
}