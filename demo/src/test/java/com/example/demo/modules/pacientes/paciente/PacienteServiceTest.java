package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 27);

    @Mock private PersonaRepository personaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ExpedienteRepository expedienteRepository;
    @Mock private EpisodioRepository episodioRepository;
    @Mock private PersonaResponsableRepository responsableRepository;
    @Mock private ReferenciaRepository referenciaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private PacienteService service;

    private PacienteDTOs.PersonaRequest persona(String cui, LocalDate nacimiento) {
        return new PacienteDTOs.PersonaRequest(cui, "Ana Maria", "Lopez Ruiz", nacimiento,
                Sexo.FEMENINO, "12345678", null);
    }

    private PacienteDTOs.Request solicitud(LocalDate nacimiento, TipoAtencion tipo,
                                            PacienteDTOs.ResponsableRequest responsable,
                                            PacienteDTOs.ReferenciaRequest referencia) {
        return new PacienteDTOs.Request(
            new PacienteDTOs.PacienteRequest(persona("1234567890123", nacimiento), null, null, null),
                new PacienteDTOs.EpisodioRequest(tipo, "Ingreso", 5L, responsable, referencia));
    }

    private void prepararGuardado() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(UsuarioEntity.builder().id(5L).build()));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(invocation -> {
            PersonaEntity entity = invocation.getArgument(0);
            entity.setId(entity.getCui().equals("1234567890123") ? 10L : 11L);
            return entity;
        });
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(invocation -> {
            PacienteEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            return entity;
        });
        when(expedienteRepository.save(any(ExpedienteEntity.class))).thenAnswer(invocation -> {
            ExpedienteEntity entity = invocation.getArgument(0);
            entity.setId(30L);
            return entity;
        });
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(invocation -> {
            EpisodioEntity entity = invocation.getArgument(0);
            entity.setId(40L);
            return entity;
        });
    }

    @Test
    void emergenciaMenorRequiereResponsableAntesDeGuardar() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.crear(solicitud(HOY.minusYears(18).plusDays(1), TipoAtencion.EMERGENCIA,
                        null, null), HOY));

        assertTrue(error.getMessage().contains("responsable"));
        verifyNoInteractions(usuarioRepository, episodioRepository);
        verify(personaRepository, never()).save(any());
    }

    @Test
    void hospitalizacionRequiereResponsableAunqueSeaMayor() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(solicitud(HOY.minusYears(40), TipoAtencion.HOSPITALIZACION,
                        null, null), HOY));

        verify(episodioRepository, never()).save(any());
    }

    @Test
    void emergenciaEnSuCumpleanosDieciochoNoRequiereResponsable() {
        prepararGuardado();

        PacienteDTOs.Response response = service.crear(
                solicitud(HOY.minusYears(18), TipoAtencion.EMERGENCIA, null, null), HOY);

        assertEquals(20L, response.pacienteId());
        assertEquals(30L, response.expedienteId());
        assertEquals(40L, response.episodioId());
        assertNull(response.responsableId());
        assertNull(response.referenciaId());
        assertFalse(response.pacienteExistente());
        ArgumentCaptor<EpisodioEntity> episodio = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodio.capture());
        assertSame(episodio.getValue().getExpediente().getPaciente(), episodio.getValue().getPaciente());
        assertEquals("Ana", episodio.getValue().getPaciente().getPersona().getPrimerNombre());
        verifyNoInteractions(responsableRepository, referenciaRepository);
    }

    @Test
    void hospitalizacionGuardaResponsableYReferenciaEnElEpisodio() {
        prepararGuardado();
        when(responsableRepository.save(any(PersonaResponsableEntity.class))).thenAnswer(invocation -> {
            PersonaResponsableEntity entity = invocation.getArgument(0);
            entity.setId(50L);
            return entity;
        });
        when(referenciaRepository.save(any(ReferenciaEntity.class))).thenAnswer(invocation -> {
            ReferenciaEntity entity = invocation.getArgument(0);
            entity.setId(60L);
            return entity;
        });
        PacienteDTOs.ResponsableRequest responsable = new PacienteDTOs.ResponsableRequest(
                persona("9876543210123", HOY.minusYears(50)), "Madre", null);
        PacienteDTOs.ReferenciaRequest referencia = new PacienteDTOs.ReferenciaRequest(
                "Centro de salud", "Evaluacion", "https://example.org/documento");

        PacienteDTOs.Response response = service.crear(
                solicitud(HOY.minusYears(30), TipoAtencion.HOSPITALIZACION, responsable, referencia), HOY);

        assertEquals(50L, response.responsableId());
        assertEquals(60L, response.referenciaId());
        ArgumentCaptor<EpisodioEntity> episodio = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodio.capture());
        assertEquals("Madre", episodio.getValue().getPersonaResponsable().getParentesco());
        assertEquals("Centro de salud", episodio.getValue().getReferencia().getNombreInstitucion());
        assertEquals(5L, episodio.getValue().getMedico().getId());
    }

    @Test
    void reingresoReutilizaExpedienteYUsaEdadRegistrada() {
        PersonaEntity persona = PersonaEntity.builder().id(10L).cui("1234567890123")
                .fechaNacimiento(HOY.minusYears(25)).build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder().id(30L).paciente(paciente).build();
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(pacienteRepository.findByPersonaId(10L)).thenReturn(Optional.of(paciente));
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.of(expediente));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(UsuarioEntity.builder().id(5L).build()));
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(invocation -> {
            EpisodioEntity entity = invocation.getArgument(0);
            entity.setId(41L);
            return entity;
        });

        PacienteDTOs.Response response = service.crear(
                solicitud(HOY.minusYears(10), TipoAtencion.EMERGENCIA, null, null), HOY);

        assertTrue(response.pacienteExistente());
        assertEquals(20L, response.pacienteId());
        assertEquals(30L, response.expedienteId());
        assertEquals(41L, response.episodioId());
        verify(personaRepository, never()).save(any());
        verify(pacienteRepository, never()).save(any());
        verify(expedienteRepository, never()).save(any());
    }

    @Test
    void pacienteExistenteSinExpedienteRecibeUnoNuevo() {
        PersonaEntity persona = PersonaEntity.builder().id(10L).cui("1234567890123")
                .fechaNacimiento(HOY.minusYears(25)).build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).build();
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(pacienteRepository.findByPersonaId(10L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(UsuarioEntity.builder().id(5L).build()));
        when(expedienteRepository.save(any(ExpedienteEntity.class))).thenAnswer(invocation -> {
            ExpedienteEntity entity = invocation.getArgument(0);
            entity.setId(30L);
            return entity;
        });
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(invocation -> {
            EpisodioEntity entity = invocation.getArgument(0);
            entity.setId(42L);
            return entity;
        });

        PacienteDTOs.Response response = service.crear(
                solicitud(HOY.minusYears(25), TipoAtencion.EMERGENCIA, null, null), HOY);

        assertTrue(response.pacienteExistente());
        assertEquals(30L, response.expedienteId());
        ArgumentCaptor<ExpedienteEntity> expediente = ArgumentCaptor.forClass(ExpedienteEntity.class);
        verify(expedienteRepository).save(expediente.capture());
        assertSame(paciente, expediente.getValue().getPaciente());
        ArgumentCaptor<EpisodioEntity> episodio = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodio.capture());
        assertSame(paciente, episodio.getValue().getExpediente().getPaciente());
        verify(personaRepository, never()).save(any());
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void pacienteExistenteMenorNoPuedeEvadirReglaConFechaEnSolicitud() {
        PersonaEntity persona = PersonaEntity.builder().id(10L).cui("1234567890123")
                .fechaNacimiento(HOY.minusYears(17)).build();
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(pacienteRepository.findByPersonaId(10L)).thenReturn(Optional.of(
                PacienteEntity.builder().id(20L).persona(persona).build()));

        assertThrows(IllegalArgumentException.class,
                () -> service.crear(solicitud(HOY.minusYears(30), TipoAtencion.EMERGENCIA, null, null), HOY));

        verify(episodioRepository, never()).save(any());
    }
}