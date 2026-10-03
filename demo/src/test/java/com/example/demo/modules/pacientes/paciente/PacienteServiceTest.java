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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
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
    @Mock private ReferenciaArchivoStorage referenciaArchivoStorage;

    @InjectMocks private PacienteService service;

    private PacienteDTOs.PersonaRequest persona(String cui, LocalDate nacimiento) {
        return new PacienteDTOs.PersonaRequest(cui, "Ana Maria", "Lopez Ruiz", nacimiento,
                Sexo.FEMENINO, "12345678", null);
    }

    private PacienteDTOs.Request solicitud(LocalDate nacimiento, TipoAtencion tipo,
                                            PacienteDTOs.ResponsableRequest responsable,
                                            PacienteDTOs.ReferenciaRequest referencia) {
        return new PacienteDTOs.Request(
            new PacienteDTOs.PacienteRequest(persona("1234567890123", nacimiento), null, null, null, null),
                new PacienteDTOs.EpisodioRequest(tipo, "Ingreso", 5L, responsable, referencia));
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
            new PacienteDTOs.PacienteRequest(personaSinDatosOpcionales, null, null, null, null),
            new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null));

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
        String ruta = "uploads/referencias/expediente-30-uuid.pdf";
        when(referenciaArchivoStorage.guardar(archivo, 30L)).thenReturn(ruta);
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
        assertEquals(ruta, episodio.getValue().getReferencia().getUrlDocumento());
        assertEquals(5L, episodio.getValue().getMedico().getId());
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
        when(camasRepository.findById(9L)).thenReturn(Optional.of(cama));
        when(camasRepository.save(cama)).thenReturn(cama);
        PacienteDTOs.PersonaRequest datosPersona = persona("1234567890123", HOY.minusYears(30));
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(datosPersona, null, null, null, 9L),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null));

        service.crear(request, HOY);

        assertEquals(20L, cama.getPaciente().getId());
        assertEquals(EstadoCama.OCUPADA, cama.getEstado());
        verify(camasRepository).save(cama);
    }

    @Test
    void crearRechazaCamaNoDisponibleAntesDeGuardarPaciente() {
        camasEntity cama = new camasEntity();
        cama.setId(9L);
        cama.setEstado(EstadoCama.LIMPIEZA);
        cama.setActivo(true);
        when(camasRepository.findById(9L)).thenReturn(Optional.of(cama));
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(persona("1234567890123", HOY.minusYears(30)),
                        null, null, null, 9L),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null));

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
        EpisodioEntity episodioReciente = EpisodioEntity.builder()
            .id(42L).paciente(paciente).tipoAtencion(TipoAtencion.EMERGENCIA).build();
        EpisodioEntity episodioAnterior = EpisodioEntity.builder()
            .id(41L).paciente(paciente).tipoAtencion(TipoAtencion.HOSPITALIZACION).build();
        when(pacienteRepository.findByEstadoOrderByIdAsc(true)).thenReturn(List.of(paciente));
        when(expedienteRepository.findByPaciente_IdIn(List.of(20L))).thenReturn(List.of(expediente));
        when(episodioRepository.findByPaciente_IdInOrderByIdDesc(List.of(20L)))
            .thenReturn(List.of(episodioReciente, episodioAnterior));

        List<PacienteDTOs.ListadoResponse> resultado = service.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("EXP-30", resultado.get(0).codigoExpediente());
        assertEquals("Ana Maria Luisa Lopez Ruiz", resultado.get(0).nombreCompleto());
        assertEquals("12345678", resultado.get(0).telefono());
        assertEquals(TipoAtencion.EMERGENCIA, resultado.get(0).tipoTratamiento());
        assertTrue(resultado.get(0).estado());
        verify(pacienteRepository).findByEstadoOrderByIdAsc(true);
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
}