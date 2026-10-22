package com.example.demo.modules.enfermeria.notas;

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
class notaEnfermeriaServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Mock
    private notaEnfermeriaRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private notaEnfermeriaService service;

    @Test
    void crear_debeGuardarLaNota() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(notaEnfermeriaEntity.class))).thenAnswer(invocation -> {
            notaEnfermeriaEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        notaEnfermeriaDTOs.Response response = service.crear(new notaEnfermeriaDTOs.Request(
                "  Paciente estable ", 1L, 2L));

        assertEquals(10L, response.id());
        assertEquals("Paciente estable", response.texto());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertFalse(response.eliminado());
    }

    @Test
    void actualizar_debeCambiarElTexto() {
        notaEnfermeriaEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(notaEnfermeriaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notaEnfermeriaDTOs.Response response = service.actualizar(10L, new notaEnfermeriaDTOs.Request(
                "Paciente con mejoría", 1L, 3L));

        assertEquals("Paciente con mejoría", response.texto());
        assertEquals("Ana Lopez", response.usuarioNombre());
    }

    @Test
    void buscarPorEpisodio_debeConsultarLasNoEliminadas() {
        when(repository.findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(1L))
                .thenReturn(List.of(entidad(10L)));

        List<notaEnfermeriaDTOs.Response> resultado = service.buscarPorEpisodio(1L);

        assertEquals(1, resultado.size());
        verify(repository).findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(1L);
    }

    @Test
    void eliminar_debeMarcarComoEliminada() {
        notaEnfermeriaEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(notaEnfermeriaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(10L);

        assertTrue(existente.isEliminado());
        verify(repository).save(existente);
    }

    @Test
    void crear_debeRechazarTextoVacio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new notaEnfermeriaDTOs.Request("   ", 1L, 2L)));

        assertEquals("El texto de la nota es obligatorio", exception.getMessage());
    }

    private notaEnfermeriaEntity entidad(Long id) {
        return notaEnfermeriaEntity.builder()
                .id(id)
                .texto("Paciente estable")
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(FECHA)
                .eliminado(false)
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
