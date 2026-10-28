package com.example.demo.modules.clinica.historiaClinica;

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
class historiaClinicaServiceTest {

    @Mock
    private historiaClinicaRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private historiaClinicaService service;

    @Test
    void crear_debeGuardarConFechaDelServidorYEstadoActivo() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(historiaClinicaEntity.class))).thenAnswer(invocation -> {
            historiaClinicaEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        historiaClinicaDTOs.Response response = service.crear(
                new historiaClinicaDTOs.Request("  Paciente estable  ", 1L, 2L));

        assertEquals(10L, response.id());
        assertEquals("Paciente estable", response.descripcion());
        assertEquals(1L, response.episodioId());
        assertEquals(2L, response.usuarioId());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertNotNull(response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void crear_debeRechazarDescripcionVacia() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new historiaClinicaDTOs.Request("   ", 1L, 2L)));

        assertEquals("La descripción es obligatoria", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarEpisodioInexistente() {
        when(episodioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new historiaClinicaDTOs.Request("Control", 99L, 2L)));

        assertEquals("Episodio no encontrado con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarUsuarioInexistente() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(55L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new historiaClinicaDTOs.Request("Control", 1L, 55L)));

        assertEquals("Usuario no encontrado con el ID: 55", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeConservarLaFechaOriginal() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 1, 15, 8, 30);
        historiaClinicaEntity existente = historiaClinicaEntity.builder()
                .id(5L)
                .descripcion("Descripción anterior")
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(fechaOriginal)
                .estado(true)
                .build();

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(historiaClinicaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        historiaClinicaDTOs.Response response = service.actualizar(5L,
                new historiaClinicaDTOs.Request("  Paciente en observación ", 1L, 3L));

        assertEquals("Paciente en observación", response.descripcion());
        assertEquals(3L, response.usuarioId());
        assertEquals("Ana Lopez", response.usuarioNombre());
        assertEquals(fechaOriginal, response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void actualizar_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.actualizar(77L, new historiaClinicaDTOs.Request("Control", 1L, 2L)));

        assertEquals("Historia clínica no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Historia clínica no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeRetornarSoloLosDelEstadoSolicitado() {
        when(repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true))
                .thenReturn(List.of(entidad(4L, "Control de signos vitales", true)));

        List<historiaClinicaDTOs.Response> resultado = service.buscarPorEpisodio(1L, true);

        assertEquals(1, resultado.size());
        assertEquals("Control de signos vitales", resultado.get(0).descripcion());
        assertTrue(resultado.get(0).estado());
        verify(repository).findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true);
    }

    @Test
    void cambiarEstado_debeAlternarElEstadoDelRegistro() {
        historiaClinicaEntity existente = entidad(5L, "Paciente estable", true);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(historiaClinicaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        historiaClinicaDTOs.Response response = service.cambiarEstado(5L);

        assertFalse(response.estado());
        verify(repository).save(existente);
    }

    private historiaClinicaEntity entidad(Long id, String descripcion, boolean estado) {
        return historiaClinicaEntity.builder()
                .id(id)
                .descripcion(descripcion)
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
