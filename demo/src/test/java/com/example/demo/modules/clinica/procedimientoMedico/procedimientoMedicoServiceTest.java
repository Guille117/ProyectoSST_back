package com.example.demo.modules.clinica.procedimientoMedico;

import com.example.demo.modules.clinica.procedimiento.procedimientoEntity;
import com.example.demo.modules.clinica.procedimiento.procedimientoRepository;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class procedimientoMedicoServiceTest {

    @Mock
    private procedimientoMedicoRepository repository;
    @Mock
    private procedimientoRepository procedimientoRepository;
    @Mock
    private EpisodioRepository episodioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private medicamentoLogRepository medicamentoLogRepository;
    @Mock
    private insumosLogRepository insumosLogRepository;
    @Mock
    private LoteRepository loteRepository;
    @Mock
    private SolicitudSalidaService solicitudSalidaService;

    @InjectMocks
    private procedimientoMedicoService service;

    @Test
    void crear_conResultadoYFin_debeRegistrarProcedimientoRealizadoConSusRecursos() {
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipoProcedimiento(1L, "Sutura")));
        when(episodioRepository.findById(2L)).thenReturn(Optional.of(new EpisodioEntity()));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Dra. Ana López")));
        when(medicamentoLogRepository.findByIdAndEstado(4L, true)).thenReturn(Optional.of(medicamento(4L)));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(10L);
        when(repository.save(any(procedimientoMedicoEntity.class))).thenAnswer(invocation -> {
            procedimientoMedicoEntity entity = invocation.getArgument(0);
            entity.setId(8L);
            return entity;
        });

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Sutura de herida en el antebrazo.",
                "Sin complicaciones.",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                LocalDateTime.of(2026, 10, 19, 12, 0),
                2L,
                3L,
                List.of(new procedimientoMedicoDTOs.RecursoRequest(
                        TipoRecursoProcedimiento.MEDICAMENTO, 4L, 2)));

        procedimientoMedicoDTOs.Response response = service.crear(request);

        assertEquals(8L, response.id());
        assertEquals("Sutura", response.tipoProcedimientoNombre());
        assertEquals(EstadoProcedimientoMedico.REALIZADO, response.estado());
        assertEquals("Dra. Ana López", response.usuarioNombre());
        assertEquals(1, response.recursos().size());
        assertEquals("Amoxicilina Bayer tabletas de 500 mg", response.recursos().getFirst().nombre());
        assertEquals(2, response.recursos().getFirst().cantidad());
    }

    @Test
    void crear_sinResultadoNiFin_debeRegistrarProcedimientoPendiente() {
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipoProcedimiento(1L, "Biopsia")));
        when(episodioRepository.findById(2L)).thenReturn(Optional.of(new EpisodioEntity()));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Dr. Carlos Méndez")));
        when(repository.save(any(procedimientoMedicoEntity.class))).thenAnswer(invocation -> {
            procedimientoMedicoEntity entity = invocation.getArgument(0);
            entity.setId(9L);
            return entity;
        });

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Toma de muestra para estudio histopatológico.",
                "",
                LocalDateTime.of(2026, 10, 20, 8, 0),
                null,
                2L,
                3L,
                List.of());

        procedimientoMedicoDTOs.Response response = service.crear(request);

        assertEquals(EstadoProcedimientoMedico.PENDIENTE, response.estado());
        assertEquals(0, response.recursos().size());
    }

    @Test
    void eliminar_debeMarcarElProcedimientoComoEliminado() {
        procedimientoMedicoEntity existente = procedimientoMedicoEntity.builder()
                .id(5L)
                .estado(EstadoProcedimientoMedico.REALIZADO)
                .episodio(EpisodioEntity.builder().id(2L).estado(true).build())
                .recursos(List.of())
                .build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.eliminar(5L);

        assertEquals(EstadoProcedimientoMedico.ELIMINADO, existente.getEstado());
        verify(solicitudSalidaService).cancelarPorProcedimiento(5L);
    }

    @Test
    void buscarRecursos_deMedicamento_devuelveSoloLosQueTienenExistencia() {
        medicamentoLogEntity conExistencia = medicamento(4L);
        medicamentoLogEntity sinExistencia = medicamento(5L);
        sinExistencia.setNombre("Ibuprofeno");
        when(medicamentoLogRepository.findByEstado(true)).thenReturn(List.of(conExistencia, sinExistencia));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(10L);
        when(loteRepository.sumDisponibleMedicamento(eq(5L), any(LocalDate.class))).thenReturn(0L);

        var recursos = service.buscarRecursos(TipoRecursoProcedimiento.MEDICAMENTO, "");

        assertEquals(1, recursos.size());
        assertEquals(4L, recursos.getFirst().id());
        assertEquals(10, recursos.getFirst().disponible());
    }

    @Test
    void crear_conEpisodioCerrado_debeRechazarElRegistro() {
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipoProcedimiento(1L, "Sutura")));
        when(episodioRepository.findById(2L)).thenReturn(Optional.of(
                EpisodioEntity.builder().id(2L).estado(false).build()));

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Sutura de herida.",
                "",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                null,
                2L,
                3L,
                List.of());

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_conRecursosHistoricos_debePermitirCambiarLaDescripcion() {
        procedimientoEntity tipo = tipoProcedimiento(1L, "Sutura");
        procedimientoMedicoEntity existente = procedimientoMedicoEntity.builder()
                .id(5L)
                .procedimiento(tipo)
                .descripcion("Descripción anterior")
                .resultado("Sin complicaciones.")
                .inicio(LocalDateTime.of(2026, 10, 19, 11, 0))
                .fin(LocalDateTime.of(2026, 10, 19, 12, 0))
                .estado(EstadoProcedimientoMedico.REALIZADO)
                .episodio(EpisodioEntity.builder().id(2L).estado(true).build())
                .usuario(usuario(3L, "Dra. Ana López"))
                .recursos(new ArrayList<>(List.of(procedimientoMedicoRecursoEntity.builder()
                        .tipo(TipoRecursoProcedimiento.MEDICAMENTO)
                        .referenciaId(4L)
                        .nombre("Amoxicilina Bayer tabletas de 500 mg")
                        .cantidad(2)
                        .build())))
                .build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(repository.save(existente)).thenReturn(existente);

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Descripción corregida",
                "Sin complicaciones.",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                LocalDateTime.of(2026, 10, 19, 12, 0),
                2L,
                3L,
                List.of(new procedimientoMedicoDTOs.RecursoRequest(
                        TipoRecursoProcedimiento.MEDICAMENTO, 4L, 2)));

        procedimientoMedicoDTOs.Response response = service.actualizar(5L, request);

        assertEquals("Descripción corregida", response.descripcion());
        assertEquals("Amoxicilina Bayer tabletas de 500 mg", response.recursos().getFirst().nombre());
        verify(medicamentoLogRepository, never()).findByIdAndEstado(any(), eq(true));
    }

    @Test
    void actualizar_noDebePermitirCambiarElEpisodioNiElUsuario() {
        procedimientoEntity tipo = tipoProcedimiento(1L, "Sutura");
        procedimientoMedicoEntity existente = procedimientoMedicoEntity.builder()
                .id(5L)
                .procedimiento(tipo)
                .episodio(EpisodioEntity.builder().id(2L).estado(true).build())
                .usuario(usuario(3L, "Dra. Ana López"))
                .recursos(List.of())
                .build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Sutura.",
                "",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                null,
                99L,
                3L,
                List.of());

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, request));
    }

    @Test
    void crear_conRecursosDuplicados_debeValidarLaCantidadTotal() {
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipoProcedimiento(1L, "Sutura")));
        when(episodioRepository.findById(2L)).thenReturn(Optional.of(
                EpisodioEntity.builder().id(2L).estado(true).build()));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Dra. Ana López")));
        when(medicamentoLogRepository.findByIdAndEstado(4L, true)).thenReturn(Optional.of(medicamento(4L)));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(10L);

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Sutura de herida.",
                "",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                null,
                2L,
                3L,
                List.of(
                        new procedimientoMedicoDTOs.RecursoRequest(
                                TipoRecursoProcedimiento.MEDICAMENTO, 4L, 6),
                        new procedimientoMedicoDTOs.RecursoRequest(
                                TipoRecursoProcedimiento.MEDICAMENTO, 4L, 6)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

    @Test
    void buscarRecursos_porMarca_usaLaBusquedaDelCatalogo() {
        when(medicamentoLogRepository.buscarDisponibles("Bayer")).thenReturn(List.of(medicamento(4L)));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(10L);

        var recursos = service.buscarRecursos(TipoRecursoProcedimiento.MEDICAMENTO, "Bayer");

        assertEquals(1, recursos.size());
        assertEquals("Amoxicilina Bayer tabletas de 500 mg", recursos.getFirst().nombre());
    }

    @Test
    void crear_debeSincronizarLaSolicitudDeSalida() {
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipoProcedimiento(1L, "Sutura")));
        when(episodioRepository.findById(2L)).thenReturn(Optional.of(
                EpisodioEntity.builder().id(2L).estado(true).build()));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, "Dra. Ana López")));
        when(repository.save(any(procedimientoMedicoEntity.class))).thenAnswer(invocation -> {
            procedimientoMedicoEntity entity = invocation.getArgument(0);
            entity.setId(8L);
            return entity;
        });

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Sutura de herida.",
                "",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                null,
                2L,
                3L,
                List.of());

        service.crear(request);

        verify(solicitudSalidaService).sincronizar(any(procedimientoMedicoEntity.class));
    }

    @Test
    void actualizar_debeConsultarCantidadesEntregadasYSincronizar() {
        procedimientoEntity tipo = tipoProcedimiento(1L, "Sutura");
        procedimientoMedicoEntity existente = procedimientoMedicoEntity.builder()
                .id(5L)
                .procedimiento(tipo)
                .descripcion("Descripción anterior")
                .resultado("Sin complicaciones.")
                .inicio(LocalDateTime.of(2026, 10, 19, 11, 0))
                .fin(LocalDateTime.of(2026, 10, 19, 12, 0))
                .estado(EstadoProcedimientoMedico.REALIZADO)
                .episodio(EpisodioEntity.builder().id(2L).estado(true).build())
                .usuario(usuario(3L, "Dra. Ana López"))
                .recursos(new ArrayList<>(List.of(procedimientoMedicoRecursoEntity.builder()
                        .tipo(TipoRecursoProcedimiento.MEDICAMENTO)
                        .referenciaId(4L)
                        .nombre("Amoxicilina Bayer tabletas de 500 mg")
                        .cantidad(2)
                        .build())))
                .build();
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(solicitudSalidaService.obtenerCantidadesEntregadas(5L)).thenReturn(Map.of());
        when(repository.save(existente)).thenReturn(existente);

        procedimientoMedicoDTOs.Request request = new procedimientoMedicoDTOs.Request(
                1L,
                "Descripción corregida",
                "Sin complicaciones.",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                LocalDateTime.of(2026, 10, 19, 12, 0),
                2L,
                3L,
                List.of(new procedimientoMedicoDTOs.RecursoRequest(
                        TipoRecursoProcedimiento.MEDICAMENTO, 4L, 2)));

        service.actualizar(5L, request);

        verify(solicitudSalidaService).obtenerCantidadesEntregadas(5L);
        verify(solicitudSalidaService).sincronizar(existente);
    }

    @Test
    void actualizar_alAumentarRecursoParcialmenteEntregadoValidaSoloElPendiente() {
        procedimientoEntity tipo = tipoProcedimiento(1L, "Sutura");
        procedimientoMedicoEntity existente = procedimientoConMedicamentoParcial(tipo);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(solicitudSalidaService.obtenerCantidadesEntregadas(5L))
                .thenReturn(Map.of("MEDICAMENTO#4", 15));
        when(medicamentoLogRepository.findByIdAndEstado(4L, true)).thenReturn(Optional.of(medicamento(4L)));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(10L);
        when(repository.save(existente)).thenReturn(existente);

        procedimientoMedicoDTOs.Response response = service.actualizar(5L, requestConMedicamento(25));

        assertEquals(25, response.recursos().getFirst().cantidad());
    }

    @Test
    void actualizar_alAumentarRecursoParcialmenteEntregadoRechazaSiElPendienteSuperaStock() {
        procedimientoEntity tipo = tipoProcedimiento(1L, "Sutura");
        procedimientoMedicoEntity existente = procedimientoConMedicamentoParcial(tipo);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(procedimientoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(solicitudSalidaService.obtenerCantidadesEntregadas(5L))
                .thenReturn(Map.of("MEDICAMENTO#4", 15));
        when(medicamentoLogRepository.findByIdAndEstado(4L, true)).thenReturn(Optional.of(medicamento(4L)));
        when(loteRepository.sumDisponibleMedicamento(eq(4L), any(LocalDate.class))).thenReturn(9L);

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(5L, requestConMedicamento(25)));
        verify(repository, never()).save(any());
    }

    private procedimientoMedicoEntity procedimientoConMedicamentoParcial(procedimientoEntity tipo) {
        return procedimientoMedicoEntity.builder()
                .id(5L)
                .procedimiento(tipo)
                .descripcion("Descripción anterior")
                .resultado("Sin complicaciones.")
                .inicio(LocalDateTime.of(2026, 10, 19, 11, 0))
                .fin(LocalDateTime.of(2026, 10, 19, 12, 0))
                .estado(EstadoProcedimientoMedico.REALIZADO)
                .episodio(EpisodioEntity.builder().id(2L).estado(true).build())
                .usuario(usuario(3L, "Dra. Ana López"))
                .recursos(new ArrayList<>(List.of(procedimientoMedicoRecursoEntity.builder()
                        .tipo(TipoRecursoProcedimiento.MEDICAMENTO)
                        .referenciaId(4L)
                        .nombre("Amoxicilina Bayer tabletas de 500 mg")
                        .cantidad(20)
                        .build())))
                .build();
    }

    private procedimientoMedicoDTOs.Request requestConMedicamento(int cantidad) {
        return new procedimientoMedicoDTOs.Request(
                1L,
                "Descripción corregida",
                "Sin complicaciones.",
                LocalDateTime.of(2026, 10, 19, 11, 0),
                LocalDateTime.of(2026, 10, 19, 12, 0),
                2L,
                3L,
                List.of(new procedimientoMedicoDTOs.RecursoRequest(
                        TipoRecursoProcedimiento.MEDICAMENTO, 4L, cantidad)));
    }

    private procedimientoEntity tipoProcedimiento(Long id, String nombre) {
        procedimientoEntity entity = new procedimientoEntity();
        entity.setId(id);
        entity.setNombre(nombre);
        entity.setEstado(true);
        return entity;
    }

    private medicamentoLogEntity medicamento(Long id) {
        com.example.demo.modules.farmacia.marca.marcaEntity marca =
                new com.example.demo.modules.farmacia.marca.marcaEntity();
        marca.setNombre("Bayer");
        com.example.demo.modules.farmacia.presentacion.presentacionEntity presentacion =
                new com.example.demo.modules.farmacia.presentacion.presentacionEntity();
        presentacion.setNombre("tabletas");
        com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity unidad =
                new com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity();
        unidad.setAbreviatura("mg");
        return medicamentoLogEntity.builder()
                .id(id)
                .nombre("Amoxicilina")
                .dosis(new java.math.BigDecimal("500"))
                .unidadMedida(unidad)
                .marca(marca)
                .presentacion(presentacion)
                .build();
    }

    private UsuarioEntity usuario(Long id, String nombre) {
        String[] partes = nombre.split(" ", 2);
        PersonaEntity persona = PersonaEntity.builder()
                .primerNombre(partes[0])
                .primerApellido(partes.length > 1 ? partes[1] : "")
                .build();
        return UsuarioEntity.builder()
                .id(id)
                .username("medico")
                .persona(persona)
                .build();
    }
}
