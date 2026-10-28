package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.pacientes.paciente.TipoAtencion;
import com.example.demo.modules.usuarios.usuarios.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class altaDTOs {

    public record TratamientoRequest(
            @NotNull(message = "El tipo de tratamiento es obligatorio")
            TipoTratamiento tipo,
            @NotBlank(message = "El nombre del tratamiento es obligatorio")
            @Size(max = 500, message = "El nombre del tratamiento no debe superar los 500 caracteres")
            String nombre,
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,
            @Size(max = 100, message = "La unidad de medida no debe superar los 100 caracteres")
            String unidadMedida,
            @NotNull(message = "La frecuencia es obligatoria")
            @Positive(message = "La frecuencia debe ser mayor a 0")
            Integer frecuencia,
            @NotNull(message = "El tipo de duración es obligatorio")
            TipoDuracion duracionTipo,
            Integer duracion,
            @Size(max = 2000, message = "Las observaciones no deben superar los 2000 caracteres")
            String observaciones,
            @Positive(message = "El medicamento debe ser un ID válido")
            Long medicamentoLogId,
            @Size(max = 1000, message = "El medicamento no registrado no debe superar los 1000 caracteres")
            String medicamentoLibre
    ) {}

    public record Request(
            @NotNull(message = "El tipo de alta es obligatorio")
            TipoAlta tipoAlta,
            @NotBlank(message = "El diagnóstico de egreso es obligatorio")
            @Size(max = 1000, message = "El diagnóstico de egreso no debe superar los 1000 caracteres")
            String diagnosticoEgreso,
            @Positive(message = "La condición de egreso debe ser un ID válido")
            Long condicionEgresoId,
            @Size(max = 1000, message = "La observación de egreso no debe superar los 1000 caracteres")
            String observacionEgreso,
            @NotNull(message = "El episodio es obligatorio")
            @Positive(message = "El episodio debe ser un ID válido")
            Long episodioId,
            @NotNull(message = "El usuario es obligatorio")
            @Positive(message = "El usuario debe ser un ID válido")
            Long usuarioId,
            ResponsableRetiro responsableRetiro,
            @Size(max = 1000, message = "El motivo del retiro no debe superar los 1000 caracteres")
            String motivoRetiro,
            @Size(max = 20, message = "No se pueden registrar más de 20 riesgos")
            List<@NotBlank @Size(max = 500) String> riesgosRetiro,
            @Positive(message = "La institución debe ser un ID válido")
            Long institucionId,
            @Size(max = 1000, message = "El motivo de referencia no debe superar los 1000 caracteres")
            String motivoReferencia,
            @Size(max = 1000, message = "Las indicaciones de traslado no deben superar los 1000 caracteres")
            String indicacionesTraslado,
            @Valid
            @Size(max = 100, message = "No se pueden registrar más de 100 tratamientos")
            List<TratamientoRequest> tratamientos
    ) {
        @AssertTrue(message = "El retiro voluntario requiere solicitante y motivo")
        public boolean isRetiroCompleto() {
            if (tipoAlta != TipoAlta.RETIRO_VOLUNTARIO) {
                return true;
            }
            return responsableRetiro != null
                    && motivoRetiro != null
                    && !motivoRetiro.isBlank();
        }

        @AssertTrue(message = "La referencia o traslado requiere institución y motivo")
        public boolean isReferenciaCompleta() {
            if (tipoAlta != TipoAlta.REFERENCIA_TRASLADO) {
                return true;
            }
            return institucionId != null
                    && motivoReferencia != null
                    && !motivoReferencia.isBlank();
        }
    }

    public record TratamientoResponse(
            TipoTratamiento tipo,
            String nombre,
            Integer cantidad,
            String unidadMedida,
            Integer frecuencia,
            TipoDuracion duracionTipo,
            Integer duracion,
            String observaciones,
            Long medicamentoLogId,
            String medicamentoLibre
    ) {}

    public record Response(
            Long id,
            TipoAlta tipoAlta,
            String diagnosticoEgreso,
            Long condicionEgresoId,
            String condicionEgresoNombre,
            String observacionEgreso,
            LocalDateTime fechaHoraEgreso,
            Long episodioId,
            Long usuarioId,
            String usuarioNombre,
            EstadoAlta estado,
            ResponsableRetiro responsableRetiro,
            String motivoRetiro,
            List<String> riesgosRetiro,
            Long institucionId,
            String institucionNombre,
            String motivoReferencia,
            String indicacionesTraslado,
            List<TratamientoResponse> tratamientos
    ) {}

    /** Resumen del alta para el listado de altas recientes de la pantalla de atención. */
    public record RecienteResponse(
            Long altaId,
            TipoAlta tipoAlta,
            LocalDateTime fechaHoraEgreso,
            String diagnosticoEgreso,
            String condicionEgresoNombre,
            String observacionEgreso,
            ResponsableRetiro responsableRetiro,
            String motivoRetiro,
            List<String> riesgosRetiro,
            String institucionNombre,
            String motivoReferencia,
            String indicacionesTraslado,
            Long episodioId,
            Long pacienteId,
            String codigoExpediente,
            String nombreCompleto,
            String cui,
            Sexo sexo,
            String telefono,
            TipoAtencion tipoAtencion,
            Integer edad
    ) {}
}
