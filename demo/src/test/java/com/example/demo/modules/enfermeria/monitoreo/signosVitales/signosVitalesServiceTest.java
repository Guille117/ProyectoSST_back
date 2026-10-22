package com.example.demo.modules.enfermeria.monitoreo.signosVitales;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class signosVitalesServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Mock
    private signosVitalesRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private signosVitalesService service;

    @Test
    void crear_debeGuardarLosSignosDelEpisodio() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(signosVitalesEntity.class))).thenAnswer(invocation -> {
            signosVitalesEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        signosVitalesDTOs.Response response = service.crear(new signosVitalesDTOs.Request(
                120, 80, 82, 18, new BigDecimal("36.8"), 98, "  Paciente estable ", 1L, 2L));

        assertEquals(10L, response.id());
        assertEquals(120, response.sistolica());
        assertEquals(80, response.diastolica());
        assertEquals(82, response.frecuenciaCardiaca());
        assertEquals(18, response.frecuenciaRespiratoria());
        assertEquals(new BigDecimal("36.8"), response.temperatura());
        assertEquals(98, response.saturacion());
        assertEquals("Paciente estable", response.observacion());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertFalse(response.eliminado());
    }

    @Test
    void actualizar_debeCambiarLosDatos() {
        signosVitalesEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(signosVitalesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        signosVitalesDTOs.Response response = service.actualizar(10L, new signosVitalesDTOs.Request(
                130, 85, 90, 20, new BigDecimal("37.2"), 96, "Control", 1L, 3L));

        assertEquals(130, response.sistolica());
        assertEquals(85, response.diastolica());
        assertEquals("Ana Lopez", response.usuarioNombre());
    }

    @Test
    void buscarPorEpisodio_debeConsultarLosNoEliminados() {
        when(repository.findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(1L))
                .thenReturn(List.of(entidad(10L)));

        List<signosVitalesDTOs.Response> resultado = service.buscarPorEpisodio(1L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        verify(repository).findByEpisodio_IdAndEliminadoFalseOrderByFechaDesc(1L);
    }

    @Test
    void eliminar_debeMarcarComoEliminado() {
        signosVitalesEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(signosVitalesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(10L);

        assertTrue(existente.isEliminado());
        verify(repository).save(existente);
    }

    @Test
    void crear_debeRechazarEpisodioInexistente() {
        when(episodioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new signosVitalesDTOs.Request(
                        120, 80, 82, 18, new BigDecimal("36.8"), 98, null, 99L, 2L)));

        assertEquals("Episodio no encontrado con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    private signosVitalesEntity entidad(Long id) {
        return signosVitalesEntity.builder()
                .id(id)
                .sistolica(120)
                .diastolica(80)
                .frecuenciaCardiaca(82)
                .frecuenciaRespiratoria(18)
                .temperatura(new BigDecimal("36.8"))
                .saturacion(98)
                .observacion("Paciente estable")
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
