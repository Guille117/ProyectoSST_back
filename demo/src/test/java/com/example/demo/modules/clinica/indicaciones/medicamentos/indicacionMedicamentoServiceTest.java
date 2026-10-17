package com.example.demo.modules.clinica.indicaciones.medicamentos;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
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
class indicacionMedicamentoServiceTest {

    @Mock
    private indicacionMedicamentoRepository repository;

    @Mock
    private medicamentoLogRepository medicamentoLogRepository;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private indicacionMedicamentoService service;

    @Test
    void crear_debeGuardarConFechaDelServidorYEstadoActivo() {
        when(medicamentoLogRepository.findById(9L)).thenReturn(Optional.of(medicamento(9L, "Ibuprofeno")));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(indicacionMedicamentoEntity.class))).thenAnswer(invocation -> {
            indicacionMedicamentoEntity entity = invocation.getArgument(0);
            entity.setId(30L);
            return entity;
        });

        indicacionMedicamentoDTOs.Response response = service.crear(new indicacionMedicamentoDTOs.Request(
                9L, 2, 8, 7, TipoDuracion.DIAS, "  Tomar con alimentos ", 1L, 2L));

        assertEquals(30L, response.id());
        assertEquals(9L, response.medicamentoLogId());
        assertEquals("Ibuprofeno", response.medicamento());
        assertEquals(2, response.cantidad());
        assertEquals(8, response.frecuencia());
        assertEquals(7, response.duracion());
        assertEquals(TipoDuracion.DIAS, response.tipoDuracion());
        assertEquals("Tomar con alimentos", response.observaciones());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertNotNull(response.fecha());
        assertEquals(EstadoIndicacionMedicamento.ACTIVO, response.estado());
    }

    @Test
    void crear_conDosisUnica_debeGuardarDuracionNula() {
        when(medicamentoLogRepository.findById(9L)).thenReturn(Optional.of(medicamento(9L, "Ibuprofeno")));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(indicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        indicacionMedicamentoDTOs.Response response = service.crear(new indicacionMedicamentoDTOs.Request(
                9L, 1, 24, 99, TipoDuracion.DOSIS_UNICA, null, 1L, 2L));

        assertNull(response.duracion());
        assertEquals(TipoDuracion.DOSIS_UNICA, response.tipoDuracion());
    }

    @Test
    void crear_debeRechazarDuracionNulaCuandoElTipoEsDias() {
        when(medicamentoLogRepository.findById(9L)).thenReturn(Optional.of(medicamento(9L, "Ibuprofeno")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new indicacionMedicamentoDTOs.Request(
                        9L, 2, 8, null, TipoDuracion.DIAS, null, 1L, 2L)));

        assertEquals("La duración es obligatoria cuando el tipo es DIAS", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarMedicamentoNoEncontrado() {
        when(medicamentoLogRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new indicacionMedicamentoDTOs.Request(
                        99L, 2, 8, 7, TipoDuracion.DIAS, null, 1L, 2L)));

        assertEquals("Medicamento no encontrado con el ID: 99", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarDatosYConservarLaFecha() {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 3, 5, 16, 20);
        indicacionMedicamentoEntity existente = indicacionMedicamentoEntity.builder()
                .id(6L)
                .medicamentoLog(medicamento(9L, "Ibuprofeno"))
                .cantidad(1)
                .frecuencia(12)
                .duracion(3)
                .tipoDuracion(TipoDuracion.DIAS)
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(fechaOriginal)
                .estado(EstadoIndicacionMedicamento.ACTIVO)
                .build();

        when(repository.findById(6L)).thenReturn(Optional.of(existente));
        when(medicamentoLogRepository.findById(10L)).thenReturn(Optional.of(medicamento(10L, "Paracetamol")));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Ana Lopez")));
        when(repository.save(any(indicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        indicacionMedicamentoDTOs.Response response = service.actualizar(6L, new indicacionMedicamentoDTOs.Request(
                10L, 3, 6, null, TipoDuracion.TIEMPO_INDEFINIDO, "Nuevo esquema", 1L, 3L));

        assertEquals(10L, response.medicamentoLogId());
        assertEquals("Paracetamol", response.medicamento());
        assertEquals(3, response.cantidad());
        assertNull(response.duracion());
        assertEquals(TipoDuracion.TIEMPO_INDEFINIDO, response.tipoDuracion());
        assertEquals("Ana Lopez", response.usuarioNombre());
        assertEquals(fechaOriginal, response.fecha());
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Indicación médica no encontrada con el ID: 77", exception.getMessage());
    }

    @Test
    void buscarPorEpisodio_debeFiltrarPorEpisodioYEstado() {
        when(repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, EstadoIndicacionMedicamento.ACTIVO))
                .thenReturn(List.of(entidad(7L, 2, TipoDuracion.DIAS, 7, EstadoIndicacionMedicamento.ACTIVO)));

        List<indicacionMedicamentoDTOs.Response> resultado = service.buscarPorEpisodio(1L, EstadoIndicacionMedicamento.ACTIVO);

        assertEquals(1, resultado.size());
        assertEquals(7L, resultado.get(0).id());
        verify(repository).findByEpisodio_IdAndEstadoOrderByFechaDesc(1L, EstadoIndicacionMedicamento.ACTIVO);
    }

    @Test
    void cambiarEstado_debeAsignarElNuevoEstado() {
        indicacionMedicamentoEntity existente = entidad(7L, 2, TipoDuracion.DIAS, 7, EstadoIndicacionMedicamento.ACTIVO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(indicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        indicacionMedicamentoDTOs.Response response = service.cambiarEstado(7L, EstadoIndicacionMedicamento.FINALIZADO);

        assertEquals(EstadoIndicacionMedicamento.FINALIZADO, response.estado());
        verify(repository).save(existente);
    }

    @Test
    void eliminar_debeMarcarElRegistroComoEliminado() {
        indicacionMedicamentoEntity existente = entidad(7L, 2, TipoDuracion.DIAS, 7, EstadoIndicacionMedicamento.ACTIVO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(indicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(7L);

        assertEquals(EstadoIndicacionMedicamento.ELIMINADO, existente.getEstado());
        verify(repository).save(existente);
    }

    private indicacionMedicamentoEntity entidad(Long id, Integer cantidad, TipoDuracion tipo, Integer duracion,
                                           EstadoIndicacionMedicamento estado) {
        return indicacionMedicamentoEntity.builder()
                .id(id)
                .medicamentoLog(medicamento(9L, "Ibuprofeno"))
                .cantidad(cantidad)
                .frecuencia(8)
                .duracion(duracion)
                .tipoDuracion(tipo)
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
                .estado(estado)
                .build();
    }

    private medicamentoLogEntity medicamento(Long id, String nombre) {
        return medicamentoLogEntity.builder().id(id).nombre(nombre).build();
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
