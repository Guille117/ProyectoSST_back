package com.example.demo.modules.clinica.evolucion;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
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
class evolucionServiceTest {

    @Mock
    private evolucionRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private evolucionService service;

    @Test
    void crear_debeGuardarConFechaDelServidorYEstadoActiva() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(evolucionEntity.class))).thenAnswer(invocation -> {
            evolucionEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        evolucionDTOs.Response response = service.crear(new evolucionDTOs.Request(
                "  Paciente estable  ", "  Continuar tratamiento  ", 1L, 2L));

        assertEquals(10L, response.id());
        assertEquals("Paciente estable", response.evolucionClinica());
        assertEquals("Continuar tratamiento", response.planMedico());
        assertEquals(1L, response.episodioId());
        assertEquals(2L, response.usuarioId());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertNotNull(response.fecha());
        assertEquals(EstadoEvolucion.ACTIVA, response.estado());
    }

    @Test
    void crear_debeRechazarEvolucionClinicaVacia() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new evolucionDTOs.Request("   ", "Plan", 1L, 2L)));

        assertEquals("La evolución clínica es obligatoria", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarPlanMedicoVacio() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new evolucionDTOs.Request("Evolución", "   ", 1L, 2L)));

        assertEquals("El plan médico es obligatorio", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarEpisodioInexistente() {
        when(episodioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new evolucionDTOs.Request("Evolución", "Plan", 99L, 2L)));

        assertEquals("Episodio no encontrado con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarUsuarioInexistente() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(55L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new evolucionDTOs.Request("Evolución", "Plan", 1L, 55L)));

        assertEquals("Usuario no encontrado con el ID: 55", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeConservarFechaYEstado() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 1, 15, 8, 30);
        evolucionEntity existente = entidad(5L, "Evolución anterior", "Plan anterior",
                EstadoEvolucion.ACTIVA);
        existente.setFecha(fechaOriginal);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(evolucionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        evolucionDTOs.Response response = service.actualizar(5L,
                new evolucionDTOs.Request("  Paciente en observación ", "  Nuevo plan ", 1L, 3L));

        assertEquals("Paciente en observación", response.evolucionClinica());
        assertEquals("Nuevo plan", response.planMedico());
        assertEquals(3L, response.usuarioId());
        assertEquals("Ana Lopez", response.usuarioNombre());
        assertEquals(fechaOriginal, response.fecha());
        assertEquals(EstadoEvolucion.ACTIVA, response.estado());
    }

    @Test
    void actualizar_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.actualizar(77L,
                        new evolucionDTOs.Request("Evolución", "Plan", 1L, 2L)));

        assertEquals("Evolución no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Evolución no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeRetornarSoloLosDelEstadoSolicitado() {
        when(repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, EstadoEvolucion.ACTIVA))
                .thenReturn(List.of(entidad(4L, "Control", "Plan", EstadoEvolucion.ACTIVA)));

        List<evolucionDTOs.Response> resultado =
                service.buscarPorEpisodio(1L, EstadoEvolucion.ACTIVA);

        assertEquals(1, resultado.size());
        assertEquals("Control", resultado.get(0).evolucionClinica());
        assertEquals(EstadoEvolucion.ACTIVA, resultado.get(0).estado());
        verify(repository).findByEpisodio_IdAndEstadoOrderByFechaDesc(
                1L, EstadoEvolucion.ACTIVA);
    }

    @Test
    void buscarPorEpisodio_debeRechazarEstadoNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorEpisodio(1L, null));

        assertEquals("El estado es obligatorio", exception.getMessage());
    }

    @Test
    void eliminar_debeMarcarElRegistroComoEliminado() {
        evolucionEntity existente = entidad(5L, "Evolución", "Plan", EstadoEvolucion.ACTIVA);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(evolucionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(5L);

        assertEquals(EstadoEvolucion.ELIMINADA, existente.getEstado());
        verify(repository).save(existente);
    }

    private evolucionEntity entidad(Long id, String evolucionClinica, String planMedico,
                                    EstadoEvolucion estado) {
        return evolucionEntity.builder()
                .id(id)
                .evolucionClinica(evolucionClinica)
                .planMedico(planMedico)
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 1, 15, 8, 30))
                .estado(estado)
                .build();
    }

    private EpisodioEntity episodio(Long id) {
        return EpisodioEntity.builder().id(id).descripcion("Episodio de prueba").build();
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }
}
