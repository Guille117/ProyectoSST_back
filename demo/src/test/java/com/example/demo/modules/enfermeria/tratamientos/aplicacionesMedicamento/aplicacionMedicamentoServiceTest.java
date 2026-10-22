package com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento;

import com.example.demo.modules.clinica.indicaciones.TipoDuracion;
import com.example.demo.modules.clinica.indicaciones.medicamentos.EstadoIndicacionMedicamento;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoEntity;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoRepository;
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
class aplicacionMedicamentoServiceTest {

    private static final LocalDateTime PROGRAMADA = LocalDateTime.of(2026, 10, 16, 8, 0);
    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 10, 16, 8, 5);

    @Mock
    private aplicacionMedicamentoRepository repository;

    @Mock
    private indicacionMedicamentoRepository indicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private aplicacionMedicamentoService service;

    @Test
    void crear_debeGuardarLaAplicacion() {
        when(indicacionRepository.findById(7L)).thenReturn(Optional.of(indicacion(7L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Enf. María López")));
        when(repository.existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(false);
        when(repository.save(any(aplicacionMedicamentoEntity.class))).thenAnswer(invocation -> {
            aplicacionMedicamentoEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            entity.setFechaRegistro(REGISTRO);
            return entity;
        });

        aplicacionMedicamentoDTOs.Response response = service.crear(new aplicacionMedicamentoDTOs.Request(
                7L, PROGRAMADA, EstadoAplicacionMedicamento.ADMINISTRADO, 1,
                "  Administrado sin novedad ", 2L));

        assertEquals(20L, response.id());
        assertEquals(7L, response.indicacionMedicamentoId());
        assertEquals(PROGRAMADA, response.fechaHoraProgramada());
        assertEquals(EstadoAplicacionMedicamento.ADMINISTRADO, response.estado());
        assertEquals(1, response.cantidad());
        assertEquals("Administrado sin novedad", response.observacion());
        assertEquals("Enf. María López", response.usuarioNombre());
        assertEquals(REGISTRO, response.fechaRegistro());
    }

    @Test
    void crear_debeRechazarAplicacionDuplicada() {
        when(indicacionRepository.findById(7L)).thenReturn(Optional.of(indicacion(7L)));
        when(repository.existsByIndicacion_IdAndFechaHoraProgramadaAndEliminadoFalse(7L, PROGRAMADA))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crear(new aplicacionMedicamentoDTOs.Request(
                        7L, PROGRAMADA, EstadoAplicacionMedicamento.ADMINISTRADO, 1, null, 2L)));

        assertEquals("Ya existe un registro para esa aplicación", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeCambiarEstadoYObservacion() {
        aplicacionMedicamentoEntity existente = entidad(20L);
        when(repository.findById(20L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Enf. Carlos Ramírez")));
        when(repository.save(any(aplicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        aplicacionMedicamentoDTOs.Response response = service.actualizar(20L,
                new aplicacionMedicamentoDTOs.Request(
                        7L, PROGRAMADA, EstadoAplicacionMedicamento.NO_ADMINISTRADO, null,
                        "Paciente en ayunas", 3L));

        assertEquals(EstadoAplicacionMedicamento.NO_ADMINISTRADO, response.estado());
        assertEquals("Paciente en ayunas", response.observacion());
        assertEquals("Enf. Carlos Ramírez", response.usuarioNombre());
    }

    @Test
    void buscarPorIndicacion_debeRetornarLasAplicacionesVigentes() {
        when(repository.findByIndicacion_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(7L))
                .thenReturn(List.of(entidad(20L)));

        List<aplicacionMedicamentoDTOs.Response> resultado = service.buscarPorIndicacion(7L);

        assertEquals(1, resultado.size());
        verify(repository).findByIndicacion_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(7L);
    }

    @Test
    void eliminar_debeMarcarComoEliminada() {
        aplicacionMedicamentoEntity existente = entidad(20L);
        when(repository.findById(20L)).thenReturn(Optional.of(existente));
        when(repository.save(any(aplicacionMedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(20L);

        assertTrue(existente.isEliminado());
        verify(repository).save(existente);
    }

    private aplicacionMedicamentoEntity entidad(Long id) {
        return aplicacionMedicamentoEntity.builder()
                .id(id)
                .indicacion(indicacion(7L))
                .fechaHoraProgramada(PROGRAMADA)
                .estado(EstadoAplicacionMedicamento.ADMINISTRADO)
                .cantidad(1)
                .observacion("Administrado")
                .usuario(usuario(2L, "Enf. María López"))
                .fechaRegistro(REGISTRO)
                .eliminado(false)
                .build();
    }

    private indicacionMedicamentoEntity indicacion(Long id) {
        return indicacionMedicamentoEntity.builder()
                .id(id)
                .estado(EstadoIndicacionMedicamento.ACTIVO)
                .cantidad(1)
                .frecuencia(8)
                .tipoDuracion(TipoDuracion.DIAS)
                .episodio(EpisodioEntity.builder().id(1L).descripcion("Episodio de prueba").build())
                .usuario(usuario(2L, "Dr. Andrés Salazar"))
                .fecha(LocalDateTime.of(2026, 10, 16, 7, 0))
                .fechaInicio(PROGRAMADA)
                .build();
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }
}
