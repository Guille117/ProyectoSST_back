package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import com.example.demo.security.RegistroAccesoService;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.clinica.indicaciones.cuidados.EstadoCuidado;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoEntity;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoRepository;
import com.example.demo.modules.enfermeria.monitoreo.controlHidrico.controlHidricoRepository;
import com.example.demo.modules.enfermeria.monitoreo.glucosa.glucosaRepository;
import com.example.demo.modules.enfermeria.monitoreo.signosVitales.signosVitalesEntity;
import com.example.demo.modules.enfermeria.monitoreo.signosVitales.signosVitalesRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ejecucionCuidadoServiceTest {

    private static final LocalDateTime PROGRAMADA = LocalDateTime.of(2026, 10, 16, 8, 0);
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 16, 8, 5);

    @Mock
    private ejecucionCuidadoRepository repository;

    @Mock
    private cuidadoRepository cuidadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private signosVitalesRepository signosVitalesRepository;

    @Mock
    private glucosaRepository glucosaRepository;

    @Mock
    private controlHidricoRepository controlHidricoRepository;

    @Mock

    private RegistroAccesoService registroAcceso;


    @InjectMocks
    private ejecucionCuidadoService service;

    @Test
    void crear_debeGuardarLaEjecucion() {
        when(cuidadoRepository.findById(7L)).thenReturn(Optional.of(cuidado(7L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Enf. María López")));
        when(repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(false);
        when(repository.save(any(ejecucionCuidadoEntity.class))).thenAnswer(invocation -> {
            ejecucionCuidadoEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            entity.setFechaRegistro(REGISTRO);
            return entity;
        });

        ejecucionCuidadoDTOs.Response response = service.crear(new ejecucionCuidadoDTOs.Request(
                7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO, "  Realizado sin novedad ", 2L));

        assertEquals(20L, response.id());
        assertEquals(7L, response.cuidadoId());
        assertEquals(PROGRAMADA, response.fechaHoraProgramada());
        assertEquals(EstadoEjecucionCuidado.REALIZADO, response.estado());
        assertEquals("Realizado sin novedad", response.observacion());
        assertEquals("Enf. María López", response.usuarioNombre());
        assertEquals(REGISTRO, response.fechaRegistro());
    }

    @Test
    void crear_conSignosVitales_debeVincularElMonitoreo() {
        when(cuidadoRepository.findById(7L)).thenReturn(Optional.of(cuidado(7L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Enf. María López")));
        when(signosVitalesRepository.findById(30L))
                .thenReturn(Optional.of(signosVitales(30L, 1L)));
        when(repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(false);
        when(repository.save(any(ejecucionCuidadoEntity.class))).thenAnswer(invocation -> {
            ejecucionCuidadoEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            entity.setFechaRegistro(REGISTRO);
            return entity;
        });

        ejecucionCuidadoDTOs.Response response = service.crear(new ejecucionCuidadoDTOs.Request(
                7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO, "Realizado", 2L, 30L, null, null));

        assertEquals(30L, response.signosVitalesId());
        assertNull(response.glucosaId());
        assertNull(response.controlHidricoId());
    }

    @Test
    void crear_conMonitoreoDeOtroEpisodio_debeRechazar() {
        when(cuidadoRepository.findById(7L)).thenReturn(Optional.of(cuidado(7L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Enf. María López")));
        when(signosVitalesRepository.findById(30L))
                .thenReturn(Optional.of(signosVitales(30L, 2L)));
        when(repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new ejecucionCuidadoDTOs.Request(
                        7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO, null, 2L, 30L, null, null)));

        assertEquals("El registro de signos vitales no pertenece al episodio del cuidado",
                exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarEjecucionDuplicada() {
        when(cuidadoRepository.findById(7L)).thenReturn(Optional.of(cuidado(7L)));
        when(repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new ejecucionCuidadoDTOs.Request(
                        7L, PROGRAMADA, EstadoEjecucionCuidado.REALIZADO, null, 2L)));

        assertEquals("Ya existe un registro para esa ejecución", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarEstadoYObservacion() {
        ejecucionCuidadoEntity existente = entidad(20L);
        when(repository.findById(20L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Enf. Carlos Ramírez")));
        when(repository.save(any(ejecucionCuidadoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ejecucionCuidadoDTOs.Response response = service.actualizar(20L,
                new ejecucionCuidadoDTOs.Request(
                        7L, PROGRAMADA, EstadoEjecucionCuidado.NO_REALIZADO,
                        "Paciente rechaza", 3L));

        assertEquals(EstadoEjecucionCuidado.NO_REALIZADO, response.estado());
        assertEquals("Paciente rechaza", response.observacion());
        assertEquals("Enf. Carlos Ramírez", response.usuarioNombre());
    }

    @Test
    void buscarPorCuidado_debeRetornarLasEjecucionesVigentes() {
        when(repository.findByCuidado_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(7L))
                .thenReturn(List.of(entidad(20L)));

        List<ejecucionCuidadoDTOs.Response> resultado = service.buscarPorCuidado(7L);

        assertEquals(1, resultado.size());
        verify(repository).findByCuidado_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(7L);
    }

    @Test
    void eliminar_debeMarcarComoEliminada() {
        ejecucionCuidadoEntity existente = entidad(20L);
        when(repository.findById(20L)).thenReturn(Optional.of(existente));
        when(repository.save(any(ejecucionCuidadoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(20L);

        assertTrue(existente.isEliminado());
        verify(repository).save(existente);
    }

    private ejecucionCuidadoEntity entidad(Long id) {
        return ejecucionCuidadoEntity.builder()
                .id(id)
                .cuidado(cuidado(7L))
                .fechaHoraProgramada(PROGRAMADA)
                .estado(EstadoEjecucionCuidado.REALIZADO)
                .observacion("Realizado")
                .usuario(usuario(2L, "Enf. María López"))
                .fechaRegistro(REGISTRO)
                .eliminado(false)
                .build();
    }

    private signosVitalesEntity signosVitales(Long id, Long episodioId) {
        return signosVitalesEntity.builder()
                .id(id)
                .episodio(EpisodioEntity.builder().id(episodioId).descripcion("Episodio").build())
                .eliminado(false)
                .build();
    }

    private cuidadoEntity cuidado(Long id) {
        return cuidadoEntity.builder()
                .id(id)
                .nombreCuidado("Cambio de posición")
                .frecuencia(2)
                .tipoDuracion(TipoDuracion.TIEMPO_INDEFINIDO)
                .episodio(EpisodioEntity.builder().id(1L).descripcion("Episodio de prueba").build())
                .usuario(usuario(2L, "Dr. Andrés Salazar"))
                .fecha(LocalDateTime.of(2026, 10, 16, 7, 0))
                .fechaInicio(PROGRAMADA)
                .estado(EstadoCuidado.EN_PROCESO)
                .build();
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }
}
