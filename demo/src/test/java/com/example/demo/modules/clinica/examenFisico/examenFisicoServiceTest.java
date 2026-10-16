package com.example.demo.modules.clinica.examenFisico;

import com.example.demo.modules.clinica.parteCuerpo.parteCuerpoEntity;
import com.example.demo.modules.clinica.parteCuerpo.parteCuerpoRepository;
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
class examenFisicoServiceTest {

    @Mock
    private examenFisicoRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private parteCuerpoRepository parteCuerpoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private examenFisicoService service;

    @Test
    void crear_debeGuardarConParteDelCuerpoFechaDelServidorYEstadoActivo() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(parteCuerpoRepository.findById(3L)).thenReturn(Optional.of(parteCuerpo(3L, "Torax")));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(examenFisicoEntity.class))).thenAnswer(invocation -> {
            examenFisicoEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            return entity;
        });

        examenFisicoDTOs.Response response = service.crear(
                new examenFisicoDTOs.Request("  Ruidos cardiacos normales ", 1L, 3L, 2L));

        assertEquals(20L, response.id());
        assertEquals("Ruidos cardiacos normales", response.descripcion());
        assertEquals(1L, response.episodioId());
        assertEquals(3L, response.parteCuerpoId());
        assertEquals("Torax", response.parteCuerpoNombre());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertNotNull(response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void crear_debeRechazarDescripcionVacia() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new examenFisicoDTOs.Request("  ", 1L, 3L, 2L)));

        assertEquals("La descripción es obligatoria", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarParteDelCuerpoInexistente() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(parteCuerpoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new examenFisicoDTOs.Request("Inspección", 1L, 99L, 2L)));

        assertEquals("Parte del cuerpo no encontrada con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarEpisodioInexistente() {
        when(episodioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new examenFisicoDTOs.Request("Inspección", 99L, 3L, 2L)));

        assertEquals("Episodio no encontrado con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarLaParteDelCuerpoYConservarLaFecha() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 2, 20, 11, 45);
        examenFisicoEntity existente = examenFisicoEntity.builder()
                .id(8L)
                .descripcion("Descripción anterior")
                .episodio(episodio(1L))
                .parteCuerpo(parteCuerpo(3L, "Torax"))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(fechaOriginal)
                .estado(true)
                .build();

        when(repository.findById(8L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(parteCuerpoRepository.findById(4L)).thenReturn(Optional.of(parteCuerpo(4L, "Abdomen")));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(examenFisicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examenFisicoDTOs.Response response = service.actualizar(8L,
                new examenFisicoDTOs.Request("  Dolor a la palpación ", 1L, 4L, 2L));

        assertEquals("Dolor a la palpación", response.descripcion());
        assertEquals(4L, response.parteCuerpoId());
        assertEquals("Abdomen", response.parteCuerpoNombre());
        assertEquals(fechaOriginal, response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Examen físico no encontrado con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeRetornarSoloLosDelEstadoSolicitado() {
        when(repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true))
                .thenReturn(List.of(entidad(9L, "Inspección general", true)));

        List<examenFisicoDTOs.Response> resultado = service.buscarPorEpisodio(1L, true);

        assertEquals(1, resultado.size());
        assertEquals("Inspección general", resultado.get(0).descripcion());
        verify(repository).findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true);
    }

    @Test
    void buscarPorParteCuerpo_debeRetornarSoloLosDelEstadoSolicitado() {
        when(repository.findByParteCuerpo_IdAndEstadoOrderByFechaDesc(3L, true))
                .thenReturn(List.of(entidad(9L, "Palpación", true)));

        List<examenFisicoDTOs.Response> resultado = service.buscarPorParteCuerpo(3L, true);

        assertEquals(1, resultado.size());
        assertEquals("Palpación", resultado.get(0).descripcion());
        assertEquals("Torax", resultado.get(0).parteCuerpoNombre());
        verify(repository).findByParteCuerpo_IdAndEstadoOrderByFechaDesc(3L, true);
    }

    @Test
    void cambiarEstado_debeAlternarElEstadoDelRegistro() {
        examenFisicoEntity existente = entidad(9L, "Inspección general", true);
        when(repository.findById(9L)).thenReturn(Optional.of(existente));
        when(repository.save(any(examenFisicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examenFisicoDTOs.Response response = service.cambiarEstado(9L);

        assertFalse(response.estado());
        verify(repository).save(existente);
    }

    private examenFisicoEntity entidad(Long id, String descripcion, boolean estado) {
        return examenFisicoEntity.builder()
                .id(id)
                .descripcion(descripcion)
                .episodio(episodio(1L))
                .parteCuerpo(parteCuerpo(3L, "Torax"))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 2, 20, 11, 45))
                .estado(estado)
                .build();
    }

    private EpisodioEntity episodio(Long id) {
        return EpisodioEntity.builder().id(id).descripcion("Episodio de prueba").build();
    }

    private parteCuerpoEntity parteCuerpo(Long id, String nombre) {
        parteCuerpoEntity parte = new parteCuerpoEntity();
        parte.setId(id);
        parte.setNombre(nombre);
        parte.setEstado(true);
        return parte;
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }
}

