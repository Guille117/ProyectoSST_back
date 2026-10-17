package com.example.demo.modules.clinica.indicaciones.cuidados;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class cuidadoServiceTest {

    @Mock
    private cuidadoRepository repository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private cuidadoService service;

    @Test
    void crear_debeGuardarConEstadoEnProceso() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(cuidadoEntity.class))).thenAnswer(invocation -> {
            cuidadoEntity entity = invocation.getArgument(0);
            entity.setId(30L);
            return entity;
        });

        cuidadoDTOs.Response response = service.crear(new cuidadoDTOs.Request(
                "  Cambio de vendaje ", 12, 5, TipoDuracion.DIAS, "  Cada turno ", 1L, 2L));

        assertEquals(30L, response.id());
        assertEquals("Cambio de vendaje", response.nombreCuidado());
        assertEquals(12, response.frecuencia());
        assertEquals(5, response.duracion());
        assertEquals(TipoDuracion.DIAS, response.tipoDuracion());
        assertEquals("Cada turno", response.observacion());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertEquals(EstadoCuidado.EN_PROCESO, response.estado());
    }

    @Test
    void crear_debeRechazarDuracionNulaCuandoElTipoEsDias() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new cuidadoDTOs.Request(
                        "Curacion", 12, null, TipoDuracion.DIAS, null, 1L, 2L)));

        assertEquals("La duración es obligatoria cuando el tipo es DIAS", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarNombreVacio() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new cuidadoDTOs.Request(
                        "   ", 12, 5, TipoDuracion.DIAS, null, 1L, 2L)));

        assertEquals("El nombre del cuidado es obligatorio", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarLosDatos() {
        cuidadoEntity existente = entidad(6L, "Curacion", 12, 5, TipoDuracion.DIAS, EstadoCuidado.EN_PROCESO);
        when(repository.findById(6L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(cuidadoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cuidadoDTOs.Response response = service.actualizar(6L, new cuidadoDTOs.Request(
                "Cambio de sonda", 24, null, TipoDuracion.TIEMPO_INDEFINIDO, "Vigilar", 1L, 3L));

        assertEquals("Cambio de sonda", response.nombreCuidado());
        assertEquals(24, response.frecuencia());
        assertNull(response.duracion());
        assertEquals(TipoDuracion.TIEMPO_INDEFINIDO, response.tipoDuracion());
        assertEquals("Ana Lopez", response.usuarioNombre());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Cuidado no encontrado con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeFiltrarPorEpisodioYEstado() {
        when(repository.findByEpisodio_IdAndEstado(1L, EstadoCuidado.EN_PROCESO))
                .thenReturn(List.of(entidad(7L, "Curacion", 12, 5, TipoDuracion.DIAS, EstadoCuidado.EN_PROCESO)));

        List<cuidadoDTOs.Response> resultado = service.buscarPorEpisodio(1L, EstadoCuidado.EN_PROCESO);

        assertEquals(1, resultado.size());
        assertEquals("Curacion", resultado.get(0).nombreCuidado());
        verify(repository).findByEpisodio_IdAndEstado(1L, EstadoCuidado.EN_PROCESO);
    }

    @Test
    void cambiarEstado_debeAsignarElNuevoEstado() {
        cuidadoEntity existente = entidad(7L, "Curacion", 12, 5, TipoDuracion.DIAS, EstadoCuidado.EN_PROCESO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(cuidadoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cuidadoDTOs.Response response = service.cambiarEstado(7L, EstadoCuidado.COMPLETADO);

        assertEquals(EstadoCuidado.COMPLETADO, response.estado());
        verify(repository).save(existente);
    }

    @Test
    void eliminar_debeMarcarElRegistroComoEliminado() {
        cuidadoEntity existente = entidad(7L, "Curacion", 12, 5, TipoDuracion.DIAS, EstadoCuidado.EN_PROCESO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(cuidadoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(7L);

        assertEquals(EstadoCuidado.ELIMINADO, existente.getEstado());
        verify(repository).save(existente);
    }

    private cuidadoEntity entidad(Long id, String nombre, Integer frecuencia, Integer duracion,
                                  TipoDuracion tipo, EstadoCuidado estado) {
        return cuidadoEntity.builder()
                .id(id)
                .nombreCuidado(nombre)
                .frecuencia(frecuencia)
                .duracion(duracion)
                .tipoDuracion(tipo)
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
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
