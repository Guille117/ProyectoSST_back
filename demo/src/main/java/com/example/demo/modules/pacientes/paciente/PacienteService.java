package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PersonaRepository personaRepository;
    private final PacienteRepository pacienteRepository;
    private final ExpedienteRepository expedienteRepository;
    private final EpisodioRepository episodioRepository;
    private final PersonaResponsableRepository responsableRepository;
    private final ReferenciaRepository referenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PacienteDTOs.Response crear(PacienteDTOs.Request request) {
        return crear(request, LocalDate.now());
    }

    PacienteDTOs.Response crear(PacienteDTOs.Request request, LocalDate fechaActual) {
        if (request == null || request.paciente() == null || request.paciente().persona() == null
                || request.episodio() == null || request.episodio().tipoAtencion() == null) {
            throw new IllegalArgumentException("Los datos del paciente y del episodio son obligatorios");
        }

        PacienteDTOs.PersonaRequest datosPersona = request.paciente().persona();
        PacienteDTOs.EpisodioRequest datosEpisodio = request.episodio();
        String cui = StringNormalizer.normalizarTexto(datosPersona.cui());
        Optional<PersonaEntity> personaExistente = personaRepository.findByCui(cui);
        Optional<PacienteEntity> pacienteExistente = personaExistente.flatMap(
                persona -> pacienteRepository.findByPersonaId(persona.getId()));
        LocalDate fechaNacimiento = personaExistente.map(PersonaEntity::getFechaNacimiento)
                .orElse(datosPersona.fechaNacimiento());

        if (fechaNacimiento == null || fechaNacimiento.isAfter(fechaActual)) {
            throw new IllegalArgumentException("La fecha de nacimiento del paciente no es válida");
        }
        boolean menorDeEdad = fechaNacimiento.plusYears(18).isAfter(fechaActual);
        boolean requiereResponsable = datosEpisodio.tipoAtencion() == TipoAtencion.HOSPITALIZACION
                || (datosEpisodio.tipoAtencion() == TipoAtencion.EMERGENCIA && menorDeEdad);
        if (requiereResponsable && datosEpisodio.responsable() == null) {
            throw new IllegalArgumentException("El responsable es obligatorio para esta atención");
        }
        if (datosEpisodio.responsable() != null && datosEpisodio.responsable().persona() != null
                && cui.equals(StringNormalizer.normalizarTexto(datosEpisodio.responsable().persona().cui()))) {
            throw new IllegalArgumentException("El paciente no puede ser su propio responsable");
        }

        UsuarioEntity medico = usuarioRepository.findById(datosEpisodio.medicoId())
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        PersonaEntity persona = personaExistente.orElseGet(() -> guardarPersona(datosPersona));
        PacienteEntity paciente = pacienteExistente.orElseGet(() -> pacienteRepository.save(PacienteEntity.builder()
                .persona(persona)
                .estadoCivil(request.paciente().estadoCivil())
                .direccion(StringNormalizer.normalizarNullable(request.paciente().direccion()))
                .ocupacion(StringNormalizer.normalizarNullable(request.paciente().ocupacion()))
                .build()));
        ExpedienteEntity expediente = expedienteRepository.findByPacienteId(paciente.getId())
                .orElseGet(() -> expedienteRepository.save(ExpedienteEntity.builder().paciente(paciente).build()));

        PersonaResponsableEntity responsable = guardarResponsable(datosEpisodio.responsable());
        ReferenciaEntity referencia = guardarReferencia(datosEpisodio.referencia());
        EpisodioEntity episodio = episodioRepository.save(EpisodioEntity.builder()
                .tipoAtencion(datosEpisodio.tipoAtencion())
                .descripcion(StringNormalizer.normalizarTexto(datosEpisodio.descripcion()))
                .paciente(paciente)
                .expediente(expediente)
                .medico(medico)
                .personaResponsable(responsable)
                .referencia(referencia)
                .build());

        return new PacienteDTOs.Response(paciente.getId(), expediente.getId(), episodio.getId(),
                responsable != null ? responsable.getId() : null,
                referencia != null ? referencia.getId() : null, pacienteExistente.isPresent());
    }

    private PersonaEntity guardarPersona(PacienteDTOs.PersonaRequest datos) {
        PersonaEntity persona = PersonaEntity.builder()
                .cui(StringNormalizer.normalizarTexto(datos.cui()))
                .sexo(datos.sexo())
                .fechaNacimiento(datos.fechaNacimiento())
                .telefono(StringNormalizer.normalizarNullable(datos.telefono()))
                .email(StringNormalizer.normalizarNullable(datos.email()))
                .build();
        persona.asignarNombres(datos.nombres(), datos.apellidos());
        return personaRepository.save(persona);
    }

    private PersonaResponsableEntity guardarResponsable(PacienteDTOs.ResponsableRequest datos) {
        if (datos == null) {
            return null;
        }
        String cui = StringNormalizer.normalizarTexto(datos.persona().cui());
        PersonaEntity persona = personaRepository.findByCui(cui)
                .orElseGet(() -> guardarPersona(datos.persona()));
        return responsableRepository.save(PersonaResponsableEntity.builder()
                .persona(persona)
                .parentesco(StringNormalizer.normalizarTexto(datos.parentesco()))
                .direccion(StringNormalizer.normalizarNullable(datos.direccion()))
                .build());
    }

    private ReferenciaEntity guardarReferencia(PacienteDTOs.ReferenciaRequest datos) {
        if (datos == null) {
            return null;
        }
        return referenciaRepository.save(ReferenciaEntity.builder()
                .nombreInstitucion(StringNormalizer.normalizarTexto(datos.nombreInstitucion()))
                .motivoReferencia(StringNormalizer.normalizarTexto(datos.motivoReferencia()))
                .urlDocumento(StringNormalizer.normalizarNullable(datos.urlDocumento()))
                .build());
    }
}