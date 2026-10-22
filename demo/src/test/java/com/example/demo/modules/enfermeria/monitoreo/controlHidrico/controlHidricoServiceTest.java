package com.example.demo.modules.enfermeria.monitoreo.controlHidrico;

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
class controlHidricoServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 11, 0);

    @Mock
    private controlHidricoRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private controlHidricoService service;

    @Test
    void crear_debeGuardarElRegistroHidrico() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(controlHidricoEntity.class))).thenAnswer(invocation -> {
            controlHidricoEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        controlHidricoDTOs.Response response = service.crear(new controlHidricoDTOs.Request(
                TipoRegistroHidrico.INGRESO, "  Oral ", 500, "  Agua ", 1L, 2L));

        assertEquals(10L, response.id());
        assertEquals(TipoRegistroHidrico.INGRESO, response.tipo());
        assertEquals("Oral", response.via());
        assertEquals(500, response.cantidad());
        assertEquals("Agua", response.descripcion());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertFalse(response.eliminado());
    }

    @Test
    void actualizar_debeCambiarLosDatos() {
        controlHidricoEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(controlHidricoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        controlHidricoDTOs.Response response = service.actualizar(10L, new controlHidricoDTOs.Request(
                TipoRegistroHidrico.EGRESO, "Sonda vesical", 400, "Orina", 1L, 3L));

        assertEquals(TipoRegistroHidrico.EGRESO, response.tipo());
        assertEquals("Sonda vesical", response.via());
        assertEquals(400, response.cantidad());
        assertEquals("Ana Lopez", response.usuarioNombre());
    }

    @Test
    void buscarPorEpisodio_debeIncluirLosEliminadosParaElFiltro() {
        when(repository.findByEpisodio_IdOrderByFechaDesc(1L))
                .thenReturn(List.of(entidad(10L), entidad(11L)));

        List<controlHidricoDTOs.Response> resultado = service.buscarPorEpisodio(1L);

        assertEquals(2, resultado.size());
        verify(repository).findByEpisodio_IdOrderByFechaDesc(1L);
    }

    @Test
    void eliminar_debeMarcarComoEliminado() {
        controlHidricoEntity existente = entidad(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(controlHidricoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(10L);

        assertTrue(existente.isEliminado());
        verify(repository).save(existente);
    }

    @Test
    void crear_debeRechazarCantidadNoPositiva() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new controlHidricoDTOs.Request(
                        TipoRegistroHidrico.INGRESO, "Oral", 0, "Agua", 1L, 2L)));

        assertEquals("La cantidad debe ser mayor a 0", exception.getMessage());
    }

    private controlHidricoEntity entidad(Long id) {
        return controlHidricoEntity.builder()
                .id(id)
                .tipo(TipoRegistroHidrico.INGRESO)
                .via("Oral")
                .cantidad(500)
                .descripcion("Agua")
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
