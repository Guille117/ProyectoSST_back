package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.modules.pacientes.parentesco.parentescoEntity;
import com.example.demo.modules.pacientes.parentesco.parentescoRepository;
import com.example.demo.modules.pacientes.institucion.institucionEntity;
import com.example.demo.modules.pacientes.institucion.institucionRepository;
import com.example.demo.modules.pacientes.camas.camasEntity;
import com.example.demo.modules.pacientes.camas.camasRepository;
import com.example.demo.modules.pacientes.camas.EstadoCama;
import com.example.demo.modules.pacientes.alergia.alergiaEntity;
import com.example.demo.modules.pacientes.alergia.alergiaRepository;
import com.example.demo.modules.pacientes.enfermedad.enfermedadEntity;
import com.example.demo.modules.pacientes.enfermedad.enfermedadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    @Mock private parentescoRepository parentescoRepository;
    @Mock private institucionRepository institucionRepository;
    @Mock private camasRepository camasRepository;
    @Mock private alergiaRepository alergiaRepository;
    @Mock private enfermedadRepository enfermedadRepository;
    @Mock private DocumentoEpisodioService documentoEpisodioService;

    @InjectMocks private PacienteService service;

    private PacienteDTOs.PersonaRequest persona(String cui, LocalDate nacimiento) {
        return new PacienteDTOs.PersonaRequest(cui, "Ana Maria", "Lopez Ruiz", nacimiento,
                Sexo.FEMENINO, "12345678", null);
    }

    private PacienteDTOs.Request solicitud(LocalDate nacimiento, TipoAtencion tipo,
                                            PacienteDTOs.ResponsableRequest responsable,
                                            PacienteDTOs.ReferenciaRequest referencia) {
        return new PacienteDTOs.Request(
            new PacienteDTOs.PacienteRequest(null, persona("1234567890123", nacimiento), null, null, null, null, null, null),
                new PacienteDTOs.EpisodioRequest(tipo, "Ingreso", 5L, responsable, referencia),
                5L);
    }

    private void prepararGuardado() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(UsuarioEntity.builder().id(5L).build()));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(invocation -> {
            PersonaEntity entity = invocation.getArgument(0);
            entity.setId(java.util.Objects.equals(entity.getCui(), "1234567890123") ? 10L : 11L);
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
    void crear_asignaAlergiasYEnfermedadesDelCatalogo() {
        prepararGuardado();
        when(alergiaRepository.findAllById(List.of(2L, 3L)))
                .thenReturn(List.of(alergia(2L, "Penicilina"), alergia(3L, "Lactosa")));
        when(enfermedadRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(enfermedad(1L, "Diabetes")));
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona("1234567890123", HOY.minusYears(30)),
                        null, null, null, null, List.of(2L, 3L), List.of(1L)),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);

        service.crear(request, HOY);

        ArgumentCaptor<PacienteEntity> guardado = ArgumentCaptor.forClass(PacienteEntity.class);
        verify(pacienteRepository).save(guardado.capture());
        assertEquals(List.of(2L, 3L), guardado.getValue().getAlergias().stream()
                .map(alergiaEntity::getId).toList());
        assertEquals(List.of(1L), guardado.getValue().getEnfermedades().stream()
                .map(enfermedadEntity::getId).toList());
    }

    @Test
    void crear_rechazaAlergiaInexistenteAntesDeGuardar() {
        when(alergiaRepository.findAllById(List.of(99L))).thenReturn(List.of());
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona("1234567890123", HOY.minusYears(30)),
                        null, null, null, null, List.of(99L), null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.crear(request, HOY));

        assertTrue(error.getMessage().contains("Alergia no encontrada"));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void actualizar_reemplazaAlergiasYEnfermedades() {
        PersonaEntity persona = PersonaEntity.builder()
                .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
                .fechaNacimiento(HOY.minusYears(30)).primerNombre("Ana").primerApellido("Lopez")
                .build();
        PacienteEntity paciente = PacienteEntity.builder()
                .id(20L).persona(persona).estado(true)
                .alergias(new java.util.LinkedHashSet<>(List.of(alergia(9L, "Anterior"))))
                .enfermedades(new java.util.LinkedHashSet<>(List.of(enfermedad(8L, "Anterior"))))
                .build();
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(alergiaRepository.findAllById(List.of(2L)))
                .thenReturn(List.of(alergia(2L, "Penicilina")));
        when(enfermedadRepository.findAllById(List.of())).thenReturn(List.of());
        PacienteDTOs.PersonaRequest datosPersona = persona("1234567890123", HOY.minusYears(30));
        PacienteDTOs.ActualizacionRequest request = new PacienteDTOs.ActualizacionRequest(
                new PacienteDTOs.PacienteRequest(null, datosPersona, null, null, null, null,
                        List.of(2L), List.of()),
                null,
                5L);

        service.actualizar(20L, request, null);

        assertEquals(List.of(2L), paciente.getAlergias().stream()
                .map(alergiaEntity::getId).toList());
        assertTrue(paciente.getEnfermedades().isEmpty());
    }

    @Test
    void obtenerDetalle_incluyeAlergiasYEnfermedadesOrdenadas() {
        PersonaEntity persona = PersonaEntity.builder()
                .id(10L).sexo(Sexo.FEMENINO).fechaNacimiento(HOY.minusYears(30))
                .primerNombre("Ana").primerApellido("Lopez").build();
        PacienteEntity paciente = PacienteEntity.builder()
                .id(20L).persona(persona).estado(true)
                .alergias(new java.util.LinkedHashSet<>(List.of(
                        alergia(3L, "Lactosa"), alergia(2L, "Penicilina"))))
                .enfermedades(new java.util.LinkedHashSet<>(List.of(
                        enfermedad(1L, "Diabetes"), enfermedad(4L, "Asma"))))
                .build();
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
                .thenReturn(Optional.empty());
        when(episodioRepository.findFirstByPaciente_IdOrderByIdDesc(20L))
                .thenReturn(Optional.empty());
        when(camasRepository.findByPaciente_IdAndActivoTrue(20L)).thenReturn(Optional.empty());
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.empty());

        PacienteDTOs.DetalleResponse detalle = service.obtenerDetalle(20L);

        assertEquals(List.of("Lactosa", "Penicilina"), detalle.alergias().stream()
                .map(PacienteDTOs.CatalogoDetalle::nombre).toList());
        assertEquals(List.of("Asma", "Diabetes"), detalle.enfermedades().stream()
                .map(PacienteDTOs.CatalogoDetalle::nombre).toList());
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
        void pacientePuedeGuardarseSinCuiNiTelefono() {
        prepararGuardado();
        PacienteDTOs.PersonaRequest personaSinDatosOpcionales = new PacienteDTOs.PersonaRequest(
            null, "Ana", "Lopez", HOY.minusYears(30), Sexo.FEMENINO, null, null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
            new PacienteDTOs.PacienteRequest(null, personaSinDatosOpcionales, null, null, null, null, null, null),
            new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
            5L);

        service.crear(request, HOY);

        ArgumentCaptor<PersonaEntity> persona = ArgumentCaptor.forClass(PersonaEntity.class);
        verify(personaRepository).save(persona.capture());
        assertNull(persona.getValue().getCui());
        assertNull(persona.getValue().getTelefono());
        verify(personaRepository, never()).findByCui(any());
        }

        @Test
        void responsableRequiereCui() {
        PacienteDTOs.ResponsableRequest responsable = new PacienteDTOs.ResponsableRequest(
            new PacienteDTOs.PersonaRequest(null, "Carlos", "Lopez", HOY.minusYears(50),
                Sexo.MASCULINO, "12345678", null),
            7L, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crear(solicitud(HOY.minusYears(30), TipoAtencion.HOSPITALIZACION,
                responsable, null), HOY));

        assertTrue(error.getMessage().contains("CUI del responsable"));
        verify(personaRepository, never()).save(any());
        verify(responsableRepository, never()).save(any());
        verify(episodioRepository, never()).save(any());
        }

        @Test
        void responsableRequiereTelefono() {
        PacienteDTOs.ResponsableRequest responsable = new PacienteDTOs.ResponsableRequest(
            new PacienteDTOs.PersonaRequest("9876543210123", "Carlos", "Lopez",
                HOY.minusYears(50), Sexo.MASCULINO, null, null),
            7L, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crear(solicitud(HOY.minusYears(30), TipoAtencion.HOSPITALIZACION,
                responsable, null), HOY));

        assertTrue(error.getMessage().contains("teléfono del responsable"));
        verify(personaRepository, never()).save(any());
        verify(responsableRepository, never()).save(any());
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
        assertNotNull(response.fechaHoraIngreso());
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
        MockMultipartFile archivo = new MockMultipartFile(
            "archivoReferencia", "origen.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());
        when(parentescoRepository.findById(7L)).thenReturn(Optional.of(parentesco(7L, "Madre", true)));
        when(institucionRepository.findById(8L)).thenReturn(Optional.of(institucion(8L, "Centro de salud", true)));
        PacienteDTOs.ResponsableRequest responsable = new PacienteDTOs.ResponsableRequest(
            persona("9876543210123", HOY.minusYears(50)), 7L, null);
        PacienteDTOs.ReferenciaRequest referencia = new PacienteDTOs.ReferenciaRequest(
            8L, "Evaluacion");

        PacienteDTOs.Response response = service.crear(
            solicitud(HOY.minusYears(30), TipoAtencion.HOSPITALIZACION, responsable, referencia), archivo);

        assertEquals(50L, response.responsableId());
        assertEquals(60L, response.referenciaId());
        ArgumentCaptor<EpisodioEntity> episodio = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodio.capture());
        assertEquals("Madre", episodio.getValue().getPersonaResponsable().getParentesco().getNombre());
        assertEquals(8L, episodio.getValue().getReferencia().getInstitucion().getId());
        assertEquals("Centro de salud", episodio.getValue().getReferencia().getInstitucion().getNombre());
        assertEquals(5L, episodio.getValue().getMedico().getId());
        assertNotNull(episodio.getValue().getFechaHoraIngreso());
        verify(documentoEpisodioService).guardarTodos(
                any(EpisodioEntity.class), eq(5L),
                argThat(documentos -> documentos.containsKey(TipoDocumentoEpisodio.REFERENCIA)
                        && documentos.get(TipoDocumentoEpisodio.REFERENCIA).contains(archivo)));
    }

    @Test
    void hospitalizacionRechazaParentescoInactivoAntesDeGuardar() {
        PacienteDTOs.ResponsableRequest responsable = new PacienteDTOs.ResponsableRequest(
                persona("9876543210123", HOY.minusYears(50)), 7L, null);
        when(parentescoRepository.findById(7L)).thenReturn(Optional.of(parentesco(7L, "Madre", false)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(
                solicitud(HOY.minusYears(30), TipoAtencion.HOSPITALIZACION, responsable, null), HOY));

        verify(personaRepository, never()).save(any());
        verify(episodioRepository, never()).save(any());
    }

    @Test
    void crearRechazaInstitucionInactivaAntesDeGuardar() {
        PacienteDTOs.ReferenciaRequest referencia = new PacienteDTOs.ReferenciaRequest(8L, "Evaluacion");
        when(institucionRepository.findById(8L)).thenReturn(Optional.of(institucion(8L, "Centro de salud", false)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(
                solicitud(HOY.minusYears(30), TipoAtencion.EMERGENCIA, null, referencia), HOY));

        verify(personaRepository, never()).save(any());
        verify(episodioRepository, never()).save(any());
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
        when(expedienteRepository.save(expediente)).thenReturn(expediente);
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
        verify(expedienteRepository).save(expediente);
        assertEquals("EXP-30", expediente.getCodigo());
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
        verify(expedienteRepository, times(2)).save(expediente.capture());
        assertSame(paciente, expediente.getValue().getPaciente());
        assertEquals("EXP-30", expediente.getValue().getCodigo());
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

    @Test
    void crearConCamaLaAsignaYLaMarcaOcupada() {
        prepararGuardado();
        camasEntity cama = new camasEntity();
        cama.setId(9L);
        cama.setEstado(EstadoCama.DISPONIBLE);
        cama.setActivo(true);
        cama.setPrecio(new BigDecimal("175.00"));
        when(camasRepository.findById(9L)).thenReturn(Optional.of(cama));
        when(camasRepository.save(cama)).thenReturn(cama);
        PacienteDTOs.PersonaRequest datosPersona = persona("1234567890123", HOY.minusYears(30));
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, datosPersona, null, null, null, 9L, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);

        service.crear(request, HOY);

        assertEquals(20L, cama.getPaciente().getId());
        assertEquals(EstadoCama.OCUPADA, cama.getEstado());
        verify(camasRepository).save(cama);

        ArgumentCaptor<EpisodioEntity> episodioCaptor = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodioCaptor.capture());
        assertEquals(cama, episodioCaptor.getValue().getCama());
        assertEquals(new BigDecimal("175.00"), episodioCaptor.getValue().getPrecioCama());
    }

    @Test
    void crearRechazaCamaNoDisponibleAntesDeGuardarPaciente() {
        camasEntity cama = new camasEntity();
        cama.setId(9L);
        cama.setEstado(EstadoCama.LIMPIEZA);
        cama.setActivo(true);
        when(camasRepository.findById(9L)).thenReturn(Optional.of(cama));
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona("1234567890123", HOY.minusYears(30)),
                        null, null, null, 9L, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);

        assertThrows(IllegalArgumentException.class, () -> service.crear(request, HOY));
        verify(pacienteRepository, never()).save(any());
        verify(camasRepository, never()).save(any());
    }

        @Test
        void listar_debeUsarEstadoActivoPorDefectoYElEpisodioMasReciente() {
        PersonaEntity persona = PersonaEntity.builder()
            .primerNombre("Ana")
            .segundoNombre("Maria")
            .otrosNombres("Luisa")
            .primerApellido("Lopez")
            .segundoApellido("Ruiz")
            .telefono("12345678")
            .build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder()
            .id(30L).codigo("EXP-30").paciente(paciente).build();
        LocalDateTime ingresoReciente = LocalDateTime.of(2026, 10, 17, 8, 30);
        EpisodioEntity episodioReciente = EpisodioEntity.builder()
            .id(42L).paciente(paciente).tipoAtencion(TipoAtencion.EMERGENCIA)
            .fechaHoraIngreso(ingresoReciente).build();
        EpisodioEntity episodioAnterior = EpisodioEntity.builder()
            .id(41L).paciente(paciente).tipoAtencion(TipoAtencion.HOSPITALIZACION)
            .fechaHoraIngreso(LocalDateTime.of(2026, 10, 10, 9, 0)).build();
        when(pacienteRepository.findByEstadoOrderByIdAsc(true)).thenReturn(List.of(paciente));
        when(expedienteRepository.findByPaciente_IdIn(List.of(20L))).thenReturn(List.of(expediente));
        when(episodioRepository.findByPaciente_IdInOrderByIdDesc(List.of(20L)))
            .thenReturn(List.of(episodioReciente, episodioAnterior));

        List<PacienteDTOs.ListadoResponse> resultado = service.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("EXP-30", resultado.get(0).codigoExpediente());
        assertEquals(42L, resultado.get(0).episodioId());
        assertEquals(42L, resultado.get(0).ultimoEpisodioId());
        assertEquals(ingresoReciente, resultado.get(0).fechaHoraIngreso());
        assertEquals("Ana Maria Luisa Lopez Ruiz", resultado.get(0).nombreCompleto());
        assertEquals("12345678", resultado.get(0).telefono());
        assertEquals(TipoAtencion.EMERGENCIA, resultado.get(0).tipoTratamiento());
        assertTrue(resultado.get(0).estado());
        verify(pacienteRepository).findByEstadoOrderByIdAsc(true);
        }

        @Test
        void buscar_incluyeEgresadosYDevuelveCuiYEpisodioActivo() {
        PersonaEntity personaActiva = PersonaEntity.builder()
            .id(10L).cui("1234567890123").primerNombre("Ana").primerApellido("Lopez")
            .build();
        PersonaEntity personaEgresada = PersonaEntity.builder()
            .id(11L).cui("9876543210123").primerNombre("Ana").primerApellido("Perez")
            .build();
        PacienteEntity pacienteActivo = PacienteEntity.builder()
            .id(20L).persona(personaActiva).estado(true).build();
        PacienteEntity pacienteEgresado = PacienteEntity.builder()
            .id(21L).persona(personaEgresada).estado(false).build();
        ExpedienteEntity expedienteActivo = ExpedienteEntity.builder()
            .id(30L).codigo("EXP-20").paciente(pacienteActivo).build();
        ExpedienteEntity expedienteEgresado = ExpedienteEntity.builder()
            .id(31L).codigo("EXP-21").paciente(pacienteEgresado).build();
        EpisodioEntity episodioActivo = EpisodioEntity.builder()
            .id(42L).paciente(pacienteActivo).tipoAtencion(TipoAtencion.EMERGENCIA)
            .fechaHoraIngreso(LocalDateTime.of(2026, 10, 17, 8, 30)).estado(true).build();

        when(pacienteRepository.findAll()).thenReturn(List.of(pacienteActivo, pacienteEgresado));
        when(expedienteRepository.findByPaciente_IdIn(List.of(20L, 21L)))
            .thenReturn(List.of(expedienteActivo, expedienteEgresado));
        when(episodioRepository.findByPaciente_IdInOrderByIdDesc(List.of(20L, 21L)))
            .thenReturn(List.of(episodioActivo));

        List<PacienteDTOs.ListadoResponse> resultado = service.buscar("ana");

        assertEquals(2, resultado.size());
        assertEquals("1234567890123", resultado.get(0).cui());
        assertEquals(42L, resultado.get(0).episodioId());
        assertEquals(42L, resultado.get(0).ultimoEpisodioId());
        assertTrue(resultado.get(0).estado());
        assertEquals("9876543210123", resultado.get(1).cui());
        assertNull(resultado.get(1).episodioId());
        assertNull(resultado.get(1).ultimoEpisodioId());
        assertFalse(resultado.get(1).estado());
        }

        @Test
        void buscar_conTextoVacioDevuelveListaVacia() {
        assertTrue(service.buscar("   ").isEmpty());

        verifyNoInteractions(pacienteRepository);
        }

        @Test
        void crear_rechazaPacienteConEpisodioActivo() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(HOY.minusYears(30)).primerNombre("Ana").primerApellido("Lopez")
            .build();
        PacienteEntity paciente = PacienteEntity.builder()
            .id(20L).persona(persona).estado(true).build();
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(pacienteRepository.findByPersonaId(10L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.existsByPaciente_IdAndEstadoTrue(20L)).thenReturn(true);

        PacienteDTOs.Request request = solicitud(HOY.minusYears(30), TipoAtencion.EMERGENCIA, null, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crear(request, HOY));

        assertTrue(error.getMessage().contains("episodio activo"));
        verify(episodioRepository, never()).save(any());
        }

        @Test
        void crear_reutilizaPacienteSeleccionadoAunqueNoTengaCuiYLoReactiva() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).sexo(Sexo.FEMENINO).fechaNacimiento(HOY.minusYears(30))
            .primerNombre("Antiguo").primerApellido("Nombre").telefono("11111111").build();
        PacienteEntity paciente = PacienteEntity.builder()
            .id(20L).persona(persona).estado(false).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder()
            .id(30L).codigo("EXP-20").paciente(paciente).build();
        UsuarioEntity medico = UsuarioEntity.builder().id(5L)
            .persona(PersonaEntity.builder().primerNombre("Juan").primerApellido("Perez").build()).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.existsByPaciente_IdAndEstadoTrue(20L)).thenReturn(false);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(medico));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.of(expediente));
        when(expedienteRepository.save(any(ExpedienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(inv -> {
            EpisodioEntity episodio = inv.getArgument(0);
            episodio.setId(42L);
            return episodio;
        });

        PacienteDTOs.Request request = new PacienteDTOs.Request(
            new PacienteDTOs.PacienteRequest(20L, persona(null, HOY.minusYears(30)),
                null, null, null, null, null, null),
            new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Control", 5L, null, null),
            5L);

        PacienteDTOs.Response response = service.crear(request, HOY);

        assertEquals(20L, response.pacienteId());
        assertEquals(42L, response.episodioId());
        assertTrue(response.pacienteExistente());
        assertTrue(paciente.isEstado());
        assertEquals("Ana", persona.getPrimerNombre());
        assertEquals("Maria", persona.getSegundoNombre());
        assertEquals("Lopez", persona.getPrimerApellido());
        ArgumentCaptor<EpisodioEntity> episodio = ArgumentCaptor.forClass(EpisodioEntity.class);
        verify(episodioRepository).save(episodio.capture());
        assertSame(paciente, episodio.getValue().getPaciente());
        assertEquals(TipoAtencion.EMERGENCIA, episodio.getValue().getTipoAtencion());
        }

        @Test
        void obtenerDetalle_devuelvePacienteEpisodioCamaYResponsable() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(30)).telefono("12345678")
            .primerNombre("Ana").segundoNombre("Maria")
            .primerApellido("Lopez").segundoApellido("Ruiz").build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder().id(30L).codigo("EXP-30").paciente(paciente).build();
        parentescoEntity parentesco = parentesco(7L, "Madre", true);
        PersonaEntity personaResponsable = PersonaEntity.builder()
            .id(11L).cui("9876543210123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(55)).telefono("87654321")
            .primerNombre("Rosa").primerApellido("Lopez").build();
        PersonaResponsableEntity responsable = PersonaResponsableEntity.builder()
            .id(40L).persona(personaResponsable).parentesco(parentesco)
            .direccion("Casa").estado(true).build();
        institucionEntity institucion = institucion(8L, "Centro de salud", true);
        ReferenciaEntity referencia = ReferenciaEntity.builder()
            .id(50L).institucion(institucion).motivoReferencia("Evaluación").estado(true).build();
        UsuarioEntity medico = UsuarioEntity.builder().id(5L)
            .persona(PersonaEntity.builder().primerNombre("Juan").primerApellido("Perez").build()).build();
        EpisodioEntity episodio = EpisodioEntity.builder()
            .id(42L).paciente(paciente).medico(medico)
            .tipoAtencion(TipoAtencion.HOSPITALIZACION).descripcion("Ingreso")
            .fechaHoraIngreso(LocalDateTime.of(2026, 10, 10, 8, 30))
            .personaResponsable(responsable).referencia(referencia).estado(true).build();
        camasEntity cama = camasEntity.builder()
            .id(60L).codigo("A-105").paciente(paciente)
            .estado(EstadoCama.OCUPADA).activo(true).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
            .thenReturn(Optional.of(episodio));
        when(camasRepository.findByPaciente_IdAndActivoTrue(20L)).thenReturn(Optional.of(cama));
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.of(expediente));

        PacienteDTOs.DetalleResponse detalle = service.obtenerDetalle(20L);

        assertEquals(20L, detalle.pacienteId());
        assertEquals("EXP-30", detalle.codigoExpediente());
        assertEquals("Ana Maria Lopez Ruiz", detalle.nombreCompleto());
        assertEquals("Ana Maria", detalle.nombres());
        assertEquals("Lopez Ruiz", detalle.apellidos());
        assertEquals(30, detalle.edad());
        assertEquals("A-105", detalle.camaCodigo());
        assertEquals(5L, detalle.medicoId());
        assertEquals("Juan Perez", detalle.medicoNombre());
        assertEquals("Madre", detalle.responsable().parentescoNombre());
        assertEquals("Centro de salud", detalle.referencia().institucionNombre());
        }

        @Test
        void obtenerDetalle_sinEpisodioDevuelveSoloLosDatosDelPaciente() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.MASCULINO)
            .fechaNacimiento(LocalDate.now().minusYears(40))
            .primerNombre("Luis").primerApellido("Mora").build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
            .thenReturn(Optional.empty());
        when(camasRepository.findByPaciente_IdAndActivoTrue(20L)).thenReturn(Optional.empty());
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.empty());

        PacienteDTOs.DetalleResponse detalle = service.obtenerDetalle(20L);

        assertEquals("Luis Mora", detalle.nombreCompleto());
        assertNull(detalle.episodioId());
        assertNull(detalle.tipoAtencion());
        assertNull(detalle.camaId());
        assertNull(detalle.responsable());
        assertNull(detalle.referencia());
        }

        @Test
        void obtenerDetalle_usaCamaDelUltimoEpisodioSiYaNoEstaVinculada() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(30))
            .primerNombre("Ana").primerApellido("Lopez").build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();
        camasEntity cama = camasEntity.builder()
            .id(60L).codigo("A-105").estado(EstadoCama.LIMPIEZA).activo(true).build();
        EpisodioEntity ultimoEpisodio = EpisodioEntity.builder()
            .id(42L).paciente(paciente).tipoAtencion(TipoAtencion.HOSPITALIZACION)
            .descripcion("Ingreso").fechaHoraIngreso(LocalDateTime.of(2026, 10, 10, 8, 30))
            .cama(cama).estado(false).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
            .thenReturn(Optional.empty());
        when(episodioRepository.findFirstByPaciente_IdOrderByIdDesc(20L))
            .thenReturn(Optional.of(ultimoEpisodio));
        when(camasRepository.findByPaciente_IdAndActivoTrue(20L)).thenReturn(Optional.empty());
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.empty());

        PacienteDTOs.DetalleResponse detalle = service.obtenerDetalle(20L);

        assertEquals("A-105", detalle.camaCodigo());
        }

        @Test
        void obtenerDetalle_lanzaCuandoElPacienteNoExiste() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.obtenerDetalle(99L));
        verifyNoInteractions(episodioRepository, camasRepository, expedienteRepository);
        }

        @Test
        void actualizar_guardaPacienteEpisodioResponsableReferenciaYCama() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(30)).telefono("11111111")
            .primerNombre("Ana").primerApellido("Lopez").build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder().id(30L).codigo("EXP-30").paciente(paciente).build();
        parentescoEntity parentesco = parentesco(7L, "Madre", true);
        PersonaEntity personaResponsable = PersonaEntity.builder()
            .id(11L).cui("9876543210123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(55)).telefono("87654321")
            .primerNombre("Rosa").primerApellido("Lopez").build();
        PersonaResponsableEntity responsable = PersonaResponsableEntity.builder()
            .id(40L).persona(personaResponsable).parentesco(parentesco)
            .direccion("Casa").estado(true).build();
        institucionEntity institucion = institucion(8L, "Centro de salud", true);
        ReferenciaEntity referencia = ReferenciaEntity.builder()
            .id(50L).institucion(institucion).motivoReferencia("Anterior").estado(true).build();
        UsuarioEntity medico = UsuarioEntity.builder().id(5L)
            .persona(PersonaEntity.builder().primerNombre("Juan").primerApellido("Perez").build()).build();
        EpisodioEntity episodio = EpisodioEntity.builder()
            .id(42L).paciente(paciente).medico(medico)
            .tipoAtencion(TipoAtencion.EMERGENCIA).descripcion("Anterior")
            .fechaHoraIngreso(LocalDateTime.of(2026, 10, 10, 8, 30))
            .personaResponsable(responsable).referencia(referencia).estado(true).build();
        camasEntity camaActual = camasEntity.builder()
            .id(60L).codigo("A-105").paciente(paciente)
            .estado(EstadoCama.OCUPADA).activo(true).build();
        camasEntity camaNueva = camasEntity.builder()
            .id(70L).codigo("A-106").precio(new BigDecimal("200.00"))
            .estado(EstadoCama.DISPONIBLE).activo(true).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
            .thenReturn(Optional.of(episodio));
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.of(expediente));
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(medico));
        when(parentescoRepository.findById(7L)).thenReturn(Optional.of(parentesco));
        when(institucionRepository.findById(8L)).thenReturn(Optional.of(institucion));
        when(camasRepository.findById(70L)).thenReturn(Optional.of(camaNueva));
        when(camasRepository.findByPaciente_Id(20L)).thenReturn(Optional.of(camaActual));
        when(camasRepository.save(any(camasEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(responsableRepository.save(any(PersonaResponsableEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(referenciaRepository.save(any(ReferenciaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTOs.PersonaRequest datosPersona = new PacienteDTOs.PersonaRequest(
            "1234567890123", "Ana Maria", "Lopez Ruiz", LocalDate.now().minusYears(31),
            Sexo.FEMENINO, "22222222", "ana@correo.com");
        PacienteDTOs.ResponsableRequest datosResponsable = new PacienteDTOs.ResponsableRequest(
            new PacienteDTOs.PersonaRequest("9876543210123", "Rosa", "Lopez", LocalDate.now().minusYears(56),
                Sexo.FEMENINO, "33333333", null),
            7L, "Nueva casa");
        PacienteDTOs.ReferenciaRequest datosReferencia = new PacienteDTOs.ReferenciaRequest(8L, "Nueva evaluación");
        PacienteDTOs.ActualizacionRequest request = new PacienteDTOs.ActualizacionRequest(
            new PacienteDTOs.PacienteRequest(null, datosPersona, EstadoCivil.CASADO, "Nueva dirección",
                "Nueva ocupación", 70L, null, null),
            new PacienteDTOs.EpisodioRequest(TipoAtencion.HOSPITALIZACION, "Nueva descripción", 5L,
                datosResponsable, datosReferencia),
            5L);

        PacienteDTOs.Response response = service.actualizar(20L, request, null);

        assertEquals(20L, response.pacienteId());
        assertEquals(42L, response.episodioId());
        assertEquals(40L, response.responsableId());
        assertEquals("Ana", persona.getPrimerNombre());
        assertEquals("Maria", persona.getSegundoNombre());
        assertEquals("Lopez Ruiz", paciente.getPersona().getPrimerApellido() + " " + paciente.getPersona().getSegundoApellido());
        assertEquals(EstadoCivil.CASADO, paciente.getEstadoCivil());
        assertEquals("Nueva dirección", paciente.getDireccion());
        assertEquals(TipoAtencion.HOSPITALIZACION, episodio.getTipoAtencion());
        assertEquals("Nueva descripción", episodio.getDescripcion());
        assertEquals("Rosa", personaResponsable.getPrimerNombre());
        assertEquals(7L, responsable.getParentesco().getId());
        assertEquals("Nueva evaluación", referencia.getMotivoReferencia());
        assertNull(camaActual.getPaciente());
        assertEquals(EstadoCama.DISPONIBLE, camaActual.getEstado());
        assertEquals(paciente, camaNueva.getPaciente());
        assertEquals(EstadoCama.OCUPADA, camaNueva.getEstado());
        assertEquals(camaNueva, episodio.getCama());
        assertEquals(new BigDecimal("200.00"), episodio.getPrecioCama());
        }

        @Test
        void actualizar_aEmergenciaLiberaCamaYQuitaResponsableYReferencia() {
        PersonaEntity persona = PersonaEntity.builder()
            .id(10L).cui("1234567890123").sexo(Sexo.FEMENINO)
            .fechaNacimiento(LocalDate.now().minusYears(30)).telefono("11111111")
            .primerNombre("Ana").primerApellido("Lopez").build();
        PacienteEntity paciente = PacienteEntity.builder().id(20L).persona(persona).estado(true).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder().id(30L).codigo("EXP-30").paciente(paciente).build();
        PersonaEntity personaResponsable = PersonaEntity.builder()
            .id(11L).cui("9876543210123").primerNombre("Rosa").primerApellido("Lopez").build();
        PersonaResponsableEntity responsable = PersonaResponsableEntity.builder()
            .id(40L).persona(personaResponsable).parentesco(parentesco(7L, "Madre", true))
            .estado(true).build();
        institucionEntity institucion = institucion(8L, "Centro de salud", true);
        ReferenciaEntity referencia = ReferenciaEntity.builder()
            .id(50L).institucion(institucion).motivoReferencia("Anterior").estado(true).build();
        UsuarioEntity medico = UsuarioEntity.builder().id(5L)
            .persona(PersonaEntity.builder().primerNombre("Juan").primerApellido("Perez").build()).build();
        EpisodioEntity episodio = EpisodioEntity.builder()
            .id(42L).paciente(paciente).medico(medico)
            .tipoAtencion(TipoAtencion.HOSPITALIZACION).descripcion("Anterior")
            .fechaHoraIngreso(LocalDateTime.of(2026, 10, 10, 8, 30))
            .personaResponsable(responsable).referencia(referencia).estado(true).build();
        camasEntity cama = camasEntity.builder()
            .id(60L).codigo("A-105").paciente(paciente)
            .estado(EstadoCama.OCUPADA).activo(true).build();

        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(episodioRepository.findFirstByPaciente_IdAndEstadoTrueOrderByIdDesc(20L))
            .thenReturn(Optional.of(episodio));
        when(expedienteRepository.findByPacienteId(20L)).thenReturn(Optional.of(expediente));
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        when(personaRepository.save(any(PersonaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(medico));
        when(camasRepository.findByPaciente_Id(20L)).thenReturn(Optional.of(cama));
        when(camasRepository.save(any(camasEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(responsableRepository.save(any(PersonaResponsableEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(referenciaRepository.save(any(ReferenciaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(episodioRepository.save(any(EpisodioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTOs.PersonaRequest datosPersona = new PacienteDTOs.PersonaRequest(
            "1234567890123", "Ana", "Lopez", LocalDate.now().minusYears(30),
            Sexo.FEMENINO, "11111111", null);
        PacienteDTOs.ActualizacionRequest request = new PacienteDTOs.ActualizacionRequest(
            new PacienteDTOs.PacienteRequest(null, datosPersona, null, null, null, null, null, null),
            new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Control", 5L, null, null),
            5L);

        PacienteDTOs.Response response = service.actualizar(20L, request, null);

        assertNull(episodio.getPersonaResponsable());
        assertNull(episodio.getReferencia());
        assertFalse(responsable.isEstado());
        assertFalse(referencia.isEstado());
        assertNull(cama.getPaciente());
        assertEquals(EstadoCama.DISPONIBLE, cama.getEstado());
        assertNull(response.responsableId());
        assertNull(response.referenciaId());
        }

    private parentescoEntity parentesco(Long id, String nombre, boolean estado) {
        parentescoEntity parentesco = new parentescoEntity();
        parentesco.setId(id);
        parentesco.setNombre(nombre);
        parentesco.setEstado(estado);
        return parentesco;
    }

    private institucionEntity institucion(Long id, String nombre, boolean estado) {
        institucionEntity institucion = new institucionEntity();
        institucion.setId(id);
        institucion.setNombre(nombre);
        institucion.setEstado(estado);
        return institucion;
    }

    private alergiaEntity alergia(Long id, String nombre) {
        alergiaEntity alergia = new alergiaEntity();
        alergia.setId(id);
        alergia.setNombre(nombre);
        return alergia;
    }

    private enfermedadEntity enfermedad(Long id, String nombre) {
        enfermedadEntity enfermedad = new enfermedadEntity();
        enfermedad.setId(id);
        enfermedad.setNombre(nombre);
        return enfermedad;
    }
}