package com.example.demo.modules.clinica.diagnostico;

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
class diagnosticoServiceTest {

    @Mock
    private diagnosticoRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private diagnosticoService service;

    @Test
    void crear_debeGuardarConFechaDelServidorYEstadoActivo() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(diagnosticoEntity.class))).thenAnswer(invocation -> {
            diagnosticoEntity entity = invocation.getArgument(0);
            entity.setId(30L);
            return entity;
        });

        diagnosticoDTOs.Response response = service.crear(
                new diagnosticoDTOs.Request(TipoDiagnostico.PRINCIPAL, "  Neumonía leve ", 1L, 2L));

        assertEquals(30L, response.id());
        assertEquals(TipoDiagnostico.PRINCIPAL, response.tipoDiagnostico());
        assertEquals("Neumonía leve", response.descripcion());
        assertEquals(1L, response.episodioId());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertNotNull(response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void crear_debeRechazarTipoDeDiagnosticoNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new diagnosticoDTOs.Request(null, "Neumonía leve", 1L, 2L)));

        assertEquals("El tipo de diagnóstico es obligatorio", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarDescripcionVacia() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new diagnosticoDTOs.Request(TipoDiagnostico.PRINCIPAL, "  ", 1L, 2L)));

        assertEquals("La descripción es obligatoria", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarElTipoYConservarLaFecha() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 3, 5, 16, 20);
        diagnosticoEntity existente = diagnosticoEntity.builder()
                .id(6L)
                .tipoDiagnostico(TipoDiagnostico.PROVISIONAL)
                .descripcion("Descripción anterior")
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(fechaOriginal)
                .estado(true)
                .build();

        when(repository.findById(6L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(diagnosticoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        diagnosticoDTOs.Response response = service.actualizar(6L,
                new diagnosticoDTOs.Request(TipoDiagnostico.PRINCIPAL, "  Neumonía confirmada ", 1L, 3L));

        assertEquals(TipoDiagnostico.PRINCIPAL, response.tipoDiagnostico());
        assertEquals("Neumonía confirmada", response.descripcion());
        assertEquals("Ana Lopez", response.usuarioNombre());
        assertEquals(fechaOriginal, response.fecha());
        assertTrue(response.estado());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Diagnóstico no encontrado con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeRetornarSoloLosDelEstadoSolicitado() {
        when(repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true))
                .thenReturn(List.of(entidad(7L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", true)));

        List<diagnosticoDTOs.Response> resultado = service.buscarPorEpisodio(1L, true);

        assertEquals(1, resultado.size());
        assertEquals("Neumonía leve", resultado.get(0).descripcion());
        verify(repository).findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, true);
    }

    @Test
    void buscarPorTipo_debeFiltrarPorTipoYEstado() {
        when(repository.findByTipoDiagnosticoAndEstadoOrderByFechaDesc(TipoDiagnostico.ASOCIADO, true))
                .thenReturn(List.of(entidad(7L, TipoDiagnostico.ASOCIADO, "Hipertensión", true)));

        List<diagnosticoDTOs.Response> resultado = service.buscarPorTipo(TipoDiagnostico.ASOCIADO, true);

        assertEquals(1, resultado.size());
        assertEquals(TipoDiagnostico.ASOCIADO, resultado.get(0).tipoDiagnostico());
        verify(repository).findByTipoDiagnosticoAndEstadoOrderByFechaDesc(TipoDiagnostico.ASOCIADO, true);
    }

    @Test
    void cambiarEstado_debeAlternarElEstadoDelRegistro() {
        diagnosticoEntity existente = entidad(7L, TipoDiagnostico.PRINCIPAL, "Neumonía leve", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(diagnosticoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        diagnosticoDTOs.Response response = service.cambiarEstado(7L);

        assertFalse(response.estado());
        verify(repository).save(existente);
    }

    private diagnosticoEntity entidad(Long id, TipoDiagnostico tipo, String descripcion, boolean estado) {
        return diagnosticoEntity.builder()
                .id(id)
                .tipoDiagnostico(tipo)
                .descripcion(descripcion)
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
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
