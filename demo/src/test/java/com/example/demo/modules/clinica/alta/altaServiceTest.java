package com.example.demo.modules.clinica.alta;

import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoEntity;
import com.example.demo.modules.clinica.condicionEgreso.condicionEgresoRepository;
import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.pacientes.institucion.institucionRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.ExpedienteEntity;
import com.example.demo.modules.pacientes.paciente.PacienteEntity;
import com.example.demo.modules.pacientes.paciente.TipoAtencion;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class altaServiceTest {

    @Mock
    private altaRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private condicionEgresoRepository condicionEgresoRepository;

    @Mock
    private institucionRepository institucionRepository;

    @InjectMocks
    private altaService service;

    @Test
    void crear_debeGuardarConEstadoVigenteYCerrarElEpisodio() {
        EpisodioEntity episodio = episodio(1L, true);
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio));
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(condicionEgresoRepository.findById(2L)).thenReturn(Optional.of(condicion(2L, "Estable")));
        when(repository.save(any(altaEntity.class))).thenAnswer(invocation -> {
            altaEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        altaDTOs.Response response = service.crear(requestIndicacionMedica());

        assertEquals(10L, response.id());
        assertEquals(TipoAlta.INDICACION_MEDICA, response.tipoAlta());
        assertEquals("Diagnóstico de egreso", response.diagnosticoEgreso());
        assertEquals(2L, response.condicionEgresoId());
        assertEquals("Estable", response.condicionEgresoNombre());
        assertEquals(EstadoAlta.VIGENTE, response.estado());
        assertNotNull(response.fechaHoraEgreso());
        assertEquals(1, response.tratamientos().size());
        assertFalse(episodio.isEstado());
        verify(episodioRepository).save(episodio);
    }

    @Test
    void crear_debeRechazarEpisodioCerrado() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L, false)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(requestIndicacionMedica()));

        assertEquals("El episodio ya está cerrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarAltaExistente() {
        EpisodioEntity episodio = episodio(1L, true);
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio));
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(entidad(5L, EstadoAlta.VIGENTE)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(requestIndicacionMedica()));

        assertEquals("El episodio ya tiene un alta registrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_retiro_debeExigirSolicitante() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L, true)));
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(requestRetiro(null, "Motivo del retiro")));

        assertEquals("El solicitante del retiro es obligatorio", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_referencia_debeExigirInstitucion() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L, true)));
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(requestReferencia(null, "Motivo de referencia")));

        assertEquals("La institución de referencia es obligatoria", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeConservarFechaYEpisodio() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 1, 15, 8, 30);
        altaEntity existente = entidad(5L, EstadoAlta.VIGENTE);
        existente.setFechaHoraEgreso(fechaOriginal);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(condicionEgresoRepository.findById(2L)).thenReturn(Optional.of(condicion(2L, "Estable")));
        when(repository.save(any(altaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        altaDTOs.Response response = service.actualizar(5L, new altaDTOs.Request(
                TipoAlta.INDICACION_MEDICA,
                "  Diagnóstico actualizado ",
                2L,
                "Observación actualizada",
                1L,
                3L,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()));

        assertEquals("Diagnóstico actualizado", response.diagnosticoEgreso());
        assertEquals(3L, response.usuarioId());
        assertEquals("Ana Lopez", response.usuarioNombre());
        assertEquals(fechaOriginal, response.fechaHoraEgreso());
        assertEquals(1L, response.episodioId());
        verify(episodioRepository).save(existente.getEpisodio());
    }

    @Test
    void buscarPorEpisodio_debeRetornarLaAltaVigente() {
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(entidad(5L, EstadoAlta.VIGENTE)));

        List<altaDTOs.Response> resultado = service.buscarPorEpisodio(1L);

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).id());
    }

    @Test
    void anular_debeMarcarAnuladaYReabrirElEpisodio() {
        altaEntity existente = entidad(5L, EstadoAlta.VIGENTE);
        EpisodioEntity episodio = existente.getEpisodio();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(episodioRepository.existsByPaciente_IdAndEstadoTrue(1L)).thenReturn(false);
        when(repository.save(any(altaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.anular(5L);

        assertEquals(EstadoAlta.ANULADA, existente.getEstado());
        assertTrue(episodio.isEstado());
        verify(repository).save(existente);
        verify(episodioRepository).save(episodio);
    }

    @Test
    void anular_debeRechazarSiHayOtroEpisodioActivo() {
        altaEntity existente = entidad(5L, EstadoAlta.VIGENTE);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(episodioRepository.existsByPaciente_IdAndEstadoTrue(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.anular(5L));

        assertTrue(exception.getMessage().contains("otro episodio activo"));
        verify(repository, never()).save(any());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Alta no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void recientes_debeListarAltasDeLasUltimasHorasConDatosDelPaciente() {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres("Carlos Mendez", null);
        persona.setSexo(Sexo.MASCULINO);
        persona.setFechaNacimiento(LocalDate.of(1980, 1, 1));

        PacienteEntity paciente = PacienteEntity.builder().id(1L).persona(persona).build();
        ExpedienteEntity expediente = ExpedienteEntity.builder()
                .id(1L)
                .codigo("EXP-1")
                .paciente(paciente)
                .build();
        EpisodioEntity episodio = EpisodioEntity.builder()
                .id(5L)
                .tipoAtencion(TipoAtencion.HOSPITALIZACION)
                .descripcion("Episodio")
                .paciente(paciente)
                .expediente(expediente)
                .build();

        altaEntity alta = altaEntity.builder()
                .id(20L)
                .tipoAlta(TipoAlta.RETIRO_VOLUNTARIO)
                .diagnosticoEgreso("Diagnóstico de alta")
                .condicionEgreso(condicion(2L, "Estable"))
                .observacionEgreso("Observación")
                .fechaHoraEgreso(LocalDateTime.now().minusHours(2))
                .episodio(episodio)
                .usuario(usuario(2L, "Carlos Mendez"))
                .estado(EstadoAlta.VIGENTE)
                .responsableRetiro(ResponsableRetiro.PACIENTE)
                .motivoRetiro("Motivo del retiro")
                .riesgosRetiro(new ArrayList<>(List.of("Riesgo de infección")))
                .build();

        when(repository.findByEstadoAndFechaHoraEgresoGreaterThanEqualOrderByFechaHoraEgresoDesc(
                any(EstadoAlta.class), any(LocalDateTime.class)))
                .thenReturn(List.of(alta));

        List<altaDTOs.RecienteResponse> resultado = service.recientes(48);

        assertEquals(1, resultado.size());
        assertEquals(20L, resultado.get(0).altaId());
        assertEquals(TipoAlta.RETIRO_VOLUNTARIO, resultado.get(0).tipoAlta());
        assertEquals("Diagnóstico de alta", resultado.get(0).diagnosticoEgreso());
        assertEquals("Estable", resultado.get(0).condicionEgresoNombre());
        assertEquals("EXP-1", resultado.get(0).codigoExpediente());
        assertEquals("Carlos Mendez", resultado.get(0).nombreCompleto());
        assertEquals(1, resultado.get(0).riesgosRetiro().size());
        assertTrue(resultado.get(0).edad() >= 40);
    }

    @Test
    void recientes_debeRechazarHorasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> service.recientes(0));
    }

    private altaDTOs.Request requestIndicacionMedica() {
        return new altaDTOs.Request(
                TipoAlta.INDICACION_MEDICA,
                " Diagnóstico de egreso ",
                2L,
                " Observación ",
                1L,
                2L,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new altaDTOs.TratamientoRequest(
                        TipoTratamiento.MEDICAMENTO,
                        " Ibuprofeno ",
                        2,
                        " tabletas ",
                        8,
                        TipoDuracion.DIAS,
                        7,
                        " Con alimentos ",
                        9L,
                        null)));
    }

    @Test
    void crear_debeGuardarMedicamentoNoRegistrado() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L, true)));
        when(repository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                1L, EstadoAlta.VIGENTE)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(altaEntity.class))).thenAnswer(invocation -> {
            altaEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        altaDTOs.Response response = service.crear(new altaDTOs.Request(
                TipoAlta.INDICACION_MEDICA,
                "Diagnóstico",
                null,
                null,
                1L,
                2L,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new altaDTOs.TratamientoRequest(
                        TipoTratamiento.MEDICAMENTO,
                        "Aspirina",
                        1,
                        null,
                        8,
                        TipoDuracion.TIEMPO_INDEFINIDO,
                        null,
                        null,
                        null,
                        "Aspirina"))));

        assertEquals(1, response.tratamientos().size());
        assertNull(response.tratamientos().get(0).medicamentoLogId());
        assertEquals("Aspirina", response.tratamientos().get(0).medicamentoLibre());
    }

    private altaDTOs.Request requestRetiro(
            ResponsableRetiro responsable, String motivo) {
        return new altaDTOs.Request(
                TipoAlta.RETIRO_VOLUNTARIO,
                "Diagnóstico",
                null,
                null,
                1L,
                2L,
                responsable,
                motivo,
                List.of("Riesgo de infección"),
                null,
                null,
                null,
                List.of());
    }

    private altaDTOs.Request requestReferencia(Long institucionId, String motivo) {
        return new altaDTOs.Request(
                TipoAlta.REFERENCIA_TRASLADO,
                "Diagnóstico",
                null,
                null,
                1L,
                2L,
                null,
                null,
                null,
                institucionId,
                motivo,
                "Indicaciones de traslado",
                List.of());
    }

    private altaEntity entidad(Long id, EstadoAlta estado) {
        return altaEntity.builder()
                .id(id)
                .tipoAlta(TipoAlta.INDICACION_MEDICA)
                .diagnosticoEgreso("Diagnóstico")
                .fechaHoraEgreso(LocalDateTime.of(2026, 1, 15, 8, 30))
                .episodio(episodio(1L, false))
                .usuario(usuario(2L, "Carlos Mendez"))
                .estado(estado)
                .riesgosRetiro(new ArrayList<>())
                .tratamientos(new ArrayList<>())
                .build();
    }

    private EpisodioEntity episodio(Long id, boolean estado) {
        return EpisodioEntity.builder()
                .id(id)
                .descripcion("Episodio de prueba")
                .estado(estado)
                .paciente(PacienteEntity.builder().id(1L).build())
                .build();
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }

    private condicionEgresoEntity condicion(Long id, String nombre) {
        condicionEgresoEntity condicion = new condicionEgresoEntity();
        condicion.setId(id);
        condicion.setNombre(nombre);
        condicion.setEstado(true);
        return condicion;
    }
}
