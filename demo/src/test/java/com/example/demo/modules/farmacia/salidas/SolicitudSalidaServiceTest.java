package com.example.demo.modules.farmacia.salidas;

import com.example.demo.modules.clinica.procedimiento.procedimientoEntity;
import com.example.demo.modules.clinica.procedimientoMedico.EstadoProcedimientoMedico;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoEntity;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRecursoEntity;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.PacienteEntity;
import com.example.demo.modules.pacientes.paciente.TipoAtencion;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudSalidaServiceTest {

    @Mock
    private SolicitudSalidaRepository repository;
    @Mock
    private SolicitudSalidaMovimientoRepository movimientoRepository;
    @Mock
    private LoteRepository loteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private motivoBajaRepository motivoBajaRepository;

    @InjectMocks
    private SolicitudSalidaService service;

    @Test
    void sincronizar_creaSolicitudConDetallesCuandoProcedimientoTieneRecursos() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 5));
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.INSUMO, 6L, "Jeringa", 3));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.empty());
        when(repository.save(any(SolicitudSalidaEntity.class))).thenAnswer(i -> i.getArgument(0));

        service.sincronizar(procedimiento);

        ArgumentCaptor<SolicitudSalidaEntity> captor = ArgumentCaptor.forClass(SolicitudSalidaEntity.class);
        verify(repository).save(captor.capture());
        SolicitudSalidaEntity solicitud = captor.getValue();

        assertEquals("SOL-00001", solicitud.getCodigo());
        assertEquals(EstadoSolicitudSalida.PENDIENTE, solicitud.getEstado());
        assertEquals(2, solicitud.getDetalles().size());
        assertEquals(5, solicitud.getDetalles().get(0).getCantidadSolicitada());
        assertEquals(EstadoDetalleSalida.PENDIENTE, solicitud.getDetalles().get(0).getEstado());
        assertEquals(procedimiento.getEpisodio(), solicitud.getEpisodio());
        assertEquals(procedimiento.getUsuario(), solicitud.getUsuarioSolicita());
    }

    @Test
    void sincronizar_sinRecursosNoCreaSolicitud() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);
        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.empty());

        service.sincronizar(procedimiento);

        verify(repository, never()).save(any());
    }

    @Test
    void sincronizar_actualizaCantidadYAgregaNuevoRecurso() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 5));
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.INSUMO, 6L, "Jeringa", 3));

        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.PENDIENTE);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 2, 0));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.sincronizar(procedimiento);

        assertEquals(2, existente.getDetalles().size());
        assertEquals(5, existente.getDetalles().get(0).getCantidadSolicitada());
        assertEquals(3, existente.getDetalles().get(1).getCantidadSolicitada());
        assertEquals(EstadoSolicitudSalida.PENDIENTE, existente.getEstado());
    }

    @Test
    void sincronizar_rechazaReducirPorDebajoDeLoEntregado() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10));

        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 20, 15));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.of(existente));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> service.sincronizar(procedimiento));

        assertTrue(error.getMessage().contains("devol"));
        verify(repository, never()).save(any());
    }

    @Test
    void sincronizar_rechazaEliminarUnDetalleConEntregas() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);

        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 20, 15));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.sincronizar(procedimiento));
        verify(repository, never()).save(any());
    }

    @Test
    void sincronizar_eliminaDetalleSinEntregasAlQuedarseSinRecursos() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);

        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.PENDIENTE);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 5, 0));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.of(existente));
        when(movimientoRepository.existsByDetalle_Id(200L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(existente);

        service.sincronizar(procedimiento);

        assertTrue(existente.getDetalles().isEmpty());
        assertEquals(EstadoSolicitudSalida.CANCELADA, existente.getEstado());
    }

    @Test
    void sincronizar_procedimientoEliminadoNoReactivaLaSolicitud() {
        procedimientoMedicoEntity procedimiento = procedimiento(1L);
        procedimiento.setEstado(EstadoProcedimientoMedico.ELIMINADO);
        procedimiento.agregarRecurso(recurso(TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 5));

        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.CANCELADA);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 5, 0));

        when(repository.findByProcedimientoMedico_IdForUpdate(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.sincronizar(procedimiento);

        assertEquals(EstadoSolicitudSalida.CANCELADA, existente.getEstado());
        assertEquals(EstadoDetalleSalida.CANCELADA, existente.getDetalles().getFirst().getEstado());
    }

    @Test
    void obtenerCantidadesEntregadas_devuelveMapaPorTipoYReferencia() {
        SolicitudSalidaEntity existente = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        existente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 20, 15));
        existente.agregarDetalle(detalle(201L, TipoRecursoProcedimiento.INSUMO, 6L, "Jeringa", 5, 0));

        when(repository.findByProcedimientoMedico_Id(1L)).thenReturn(Optional.of(existente));

        Map<String, Integer> entregadas = service.obtenerCantidadesEntregadas(1L);

        assertEquals(Map.of("MEDICAMENTO#4", 15), entregadas);
    }

    @Test
    void entregar_parcialDescuentaLoteYActualizaEstados() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PENDIENTE);
        SolicitudSalidaDetalleEntity detalle =
                detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 0);
        solicitud.agregarDetalle(detalle);

        medicamentoLogEntity medicamento = medicamento(4L);
        LoteEntity lote = lote(50L, medicamento);
        lote.setDisponible(5);

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(loteRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(lote));
        when(movimientoRepository.save(any(SolicitudSalidaMovimientoEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(repository.save(solicitud)).thenReturn(solicitud);
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(100L))
                .thenReturn(List.of());
        when(loteRepository.findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(eq(4L), anyBoolean(), any()))
                .thenReturn(List.of(lote));

        service.entregar(100L, entrega(1L, 200L, 50L, 5));

        assertEquals(0, lote.getDisponible());
        assertEquals(5, detalle.getCantidadEntregada());
        assertEquals(EstadoDetalleSalida.PARCIAL, detalle.getEstado());
        assertEquals(EstadoSolicitudSalida.PARCIAL, solicitud.getEstado());
        verify(movimientoRepository).save(any(SolicitudSalidaMovimientoEntity.class));
    }

    @Test
    void entregar_totalMarcaSolicitudCompletada() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PENDIENTE);
        SolicitudSalidaDetalleEntity detalle =
                detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 0);
        solicitud.agregarDetalle(detalle);

        medicamentoLogEntity medicamento = medicamento(4L);
        LoteEntity lote = lote(50L, medicamento);
        lote.setDisponible(10);

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(loteRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(lote));
        when(movimientoRepository.save(any(SolicitudSalidaMovimientoEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(repository.save(solicitud)).thenReturn(solicitud);
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(100L))
                .thenReturn(List.of());

        service.entregar(100L, entrega(1L, 200L, 50L, 10));

        assertEquals(0, lote.getDisponible());
        assertEquals(10, detalle.getCantidadEntregada());
        assertEquals(EstadoDetalleSalida.COMPLETADA, detalle.getEstado());
        assertEquals(EstadoSolicitudSalida.COMPLETADA, solicitud.getEstado());
    }

    @Test
    void entregar_rechazaCantidadMayorAlPendiente() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        solicitud.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 7));

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));

        assertThrows(IllegalArgumentException.class,
                () -> service.entregar(100L, entrega(1L, 200L, 50L, 4)));
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void devolver_aptoReingresaStockYRecalcula() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        SolicitudSalidaDetalleEntity detalle =
                detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 5);
        solicitud.agregarDetalle(detalle);

        medicamentoLogEntity medicamento = medicamento(4L);
        LoteEntity lote = lote(50L, medicamento);
        lote.setDisponible(0);

        SolicitudSalidaMovimientoEntity entrega = movimiento(
                detalle, lote, TipoMovimientoSalida.ENTREGA, 5, null);

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(loteRepository.findById(50L)).thenReturn(Optional.of(lote));
        when(loteRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(lote));
        when(motivoBajaRepository.findById(7L)).thenReturn(Optional.of(motivo(7L)));
        when(movimientoRepository.findByDetalle_IdOrderByFechaHoraAsc(200L)).thenReturn(List.of(entrega));
        when(movimientoRepository.save(any(SolicitudSalidaMovimientoEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(repository.save(solicitud)).thenReturn(solicitud);
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(100L))
                .thenReturn(List.of(entrega));
        when(loteRepository.findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(eq(4L), anyBoolean(), any()))
                .thenReturn(List.of(lote));

        service.devolver(100L, devolucion(1L, 200L, 50L, 3, CondicionDevolucion.APTO));

        assertEquals(3, lote.getDisponible());
        assertEquals(2, detalle.getCantidadEntregada());
        assertEquals(EstadoDetalleSalida.PARCIAL, detalle.getEstado());
        assertEquals(EstadoSolicitudSalida.PARCIAL, solicitud.getEstado());
    }

    @Test
    void devolver_noAptoNoReingresaStockPeroReduceEntregado() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        SolicitudSalidaDetalleEntity detalle =
                detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 5);
        solicitud.agregarDetalle(detalle);

        medicamentoLogEntity medicamento = medicamento(4L);
        LoteEntity lote = lote(50L, medicamento);
        lote.setDisponible(0);

        SolicitudSalidaMovimientoEntity entrega = movimiento(
                detalle, lote, TipoMovimientoSalida.ENTREGA, 5, null);

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(loteRepository.findById(50L)).thenReturn(Optional.of(lote));
        when(motivoBajaRepository.findById(7L)).thenReturn(Optional.of(motivo(7L)));
        when(movimientoRepository.findByDetalle_IdOrderByFechaHoraAsc(200L)).thenReturn(List.of(entrega));
        when(movimientoRepository.save(any(SolicitudSalidaMovimientoEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(repository.save(solicitud)).thenReturn(solicitud);
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(100L))
                .thenReturn(List.of(entrega));
        when(loteRepository.findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(eq(4L), anyBoolean(), any()))
                .thenReturn(List.of(lote));

        service.devolver(100L, devolucion(1L, 200L, 50L, 3, CondicionDevolucion.NO_APTO));

        assertEquals(0, lote.getDisponible());
        assertEquals(2, detalle.getCantidadEntregada());
    }

    @Test
    void devolver_rechazaCantidadMayorALoEntregado() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PARCIAL);
        SolicitudSalidaDetalleEntity detalle =
                detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 5);
        solicitud.agregarDetalle(detalle);

        LoteEntity lote = lote(50L, medicamento(4L));
        SolicitudSalidaMovimientoEntity entrega = movimiento(
                detalle, lote, TipoMovimientoSalida.ENTREGA, 5, null);

        when(repository.findByIdForUpdate(100L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(loteRepository.findById(50L)).thenReturn(Optional.of(lote));
        when(motivoBajaRepository.findById(7L)).thenReturn(Optional.of(motivo(7L)));
        when(movimientoRepository.findByDetalle_IdOrderByFechaHoraAsc(200L)).thenReturn(List.of(entrega));

        assertThrows(IllegalArgumentException.class,
                () -> service.devolver(100L, devolucion(1L, 200L, 50L, 6, CondicionDevolucion.APTO)));
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void obtener_incluyeLotesDisponiblesParaElDetallePendiente() {
        SolicitudSalidaEntity solicitud = solicitud(100L, EstadoSolicitudSalida.PENDIENTE);
        solicitud.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 0));
        LoteEntity lote = lote(50L, medicamento(4L));

        when(repository.findByIdConDetalles(100L)).thenReturn(Optional.of(solicitud));
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(100L))
                .thenReturn(List.of());
        when(loteRepository.findByMedicamentoLog_IdAndEstadoAndDisponibleGreaterThan(eq(4L), anyBoolean(), any()))
                .thenReturn(List.of(lote));

        SolicitudSalidaDTOs.Response response = service.obtener(100L);

        assertEquals(1, response.detalles().size());
        assertEquals(1, response.detalles().getFirst().lotesDisponibles().size());
        assertEquals(50L, response.detalles().getFirst().lotesDisponibles().getFirst().id());
    }

    @Test
    void listar_filtraPorTextoYExcluyeCanceladasCuandoNoSeIndicaEstado() {
        SolicitudSalidaEntity pendiente = solicitud(100L, EstadoSolicitudSalida.PENDIENTE, "Ana López");
        pendiente.agregarDetalle(detalle(200L, TipoRecursoProcedimiento.MEDICAMENTO, 4L, "Amoxicilina", 10, 0));

        SolicitudSalidaEntity cancelada = solicitud(101L, EstadoSolicitudSalida.CANCELADA, "Ana López");

        when(repository.findAllByOrderByFechaSolicitudDesc()).thenReturn(List.of(pendiente, cancelada));

        List<SolicitudSalidaDTOs.ResumenResponse> resultado = service.listar(null, "ana");

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.getFirst().id());
        assertEquals(1, resultado.getFirst().medicamentos());
        assertEquals(0, resultado.getFirst().insumos());
        assertEquals(1, resultado.getFirst().pendientes());
    }

    private SolicitudSalidaDTOs.EntregaRequest entrega(
            Long usuarioId, Long detalleId, Long loteId, int cantidad) {
        return new SolicitudSalidaDTOs.EntregaRequest(usuarioId, List.of(
                new SolicitudSalidaDTOs.EntregaLineaRequest(detalleId, loteId, cantidad)));
    }

    private SolicitudSalidaDTOs.DevolucionRequest devolucion(
            Long usuarioId, Long detalleId, Long loteId, int cantidad, CondicionDevolucion condicion) {
        return new SolicitudSalidaDTOs.DevolucionRequest(usuarioId, List.of(
                new SolicitudSalidaDTOs.DevolucionLineaRequest(
                        detalleId, loteId, cantidad, condicion, 7L, null)));
    }

    private procedimientoMedicoEntity procedimiento(Long id) {
        procedimientoEntity tipo = new procedimientoEntity();
        tipo.setId(11L);
        tipo.setNombre("Sutura");
        tipo.setEstado(true);

        return procedimientoMedicoEntity.builder()
                .id(id)
                .procedimiento(tipo)
                .episodio(episodio())
                .usuario(usuario(1L))
                .recursos(new ArrayList<>())
                .build();
    }

    private SolicitudSalidaEntity solicitud(Long id, EstadoSolicitudSalida estado) {
        return solicitud(id, estado, "Ana López");
    }

    private SolicitudSalidaEntity solicitud(
            Long id, EstadoSolicitudSalida estado, String paciente) {
        return SolicitudSalidaEntity.builder()
                .id(id)
                .codigo(String.format("SOL-%05d", id))
                .procedimientoMedico(procedimiento(1L))
                .episodio(episodio(paciente))
                .usuarioSolicita(usuario(1L))
                .fechaSolicitud(LocalDateTime.of(2026, 10, 19, 10, 0))
                .estado(estado)
                .detalles(new ArrayList<>())
                .build();
    }

    private procedimientoMedicoRecursoEntity recurso(
            TipoRecursoProcedimiento tipo, Long referenciaId, String nombre, int cantidad) {
        return procedimientoMedicoRecursoEntity.builder()
                .tipo(tipo)
                .referenciaId(referenciaId)
                .nombre(nombre)
                .cantidad(cantidad)
                .build();
    }

    private SolicitudSalidaDetalleEntity detalle(
            Long id, TipoRecursoProcedimiento tipo, Long referenciaId, String nombre,
            int solicitada, int entregada) {
        SolicitudSalidaDetalleEntity detalle = SolicitudSalidaDetalleEntity.builder()
                .id(id)
                .tipo(tipo)
                .referenciaId(referenciaId)
                .nombre(nombre)
                .cantidadSolicitada(solicitada)
                .cantidadEntregada(entregada)
                .estado(entregada == 0
                        ? EstadoDetalleSalida.PENDIENTE
                        : entregada >= solicitada
                                ? EstadoDetalleSalida.COMPLETADA
                                : EstadoDetalleSalida.PARCIAL)
                .build();
        return detalle;
    }

    private LoteEntity lote(Long id, medicamentoLogEntity medicamento) {
        return LoteEntity.builder()
                .id(id)
                .codigoLote("LOTE-" + id)
                .cantidad(10)
                .disponible(10)
                .estado(true)
                .fechaVencimiento(LocalDate.now().plusMonths(6))
                .medicamentoLog(medicamento)
                .build();
    }

    private medicamentoLogEntity medicamento(Long id) {
        return medicamentoLogEntity.builder().id(id).nombre("Amoxicilina").build();
    }

    private SolicitudSalidaMovimientoEntity movimiento(
            SolicitudSalidaDetalleEntity detalle, LoteEntity lote,
            TipoMovimientoSalida tipo, int cantidad, CondicionDevolucion condicion) {
        return SolicitudSalidaMovimientoEntity.builder()
                .detalle(detalle)
                .lote(lote)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .condicion(condicion)
                .usuario(usuario(1L))
                .fechaHora(LocalDateTime.of(2026, 10, 19, 11, 0))
                .build();
    }

    private EpisodioEntity episodio() {
        return episodio("Ana López");
    }

    private EpisodioEntity episodio(String nombre) {
        String[] partes = nombre.split(" ", 2);
        PersonaEntity persona = PersonaEntity.builder()
                .id(30L)
                .primerNombre(partes[0])
                .primerApellido(partes.length > 1 ? partes[1] : "")
                .build();
        PacienteEntity paciente = PacienteEntity.builder()
                .id(20L)
                .persona(persona)
                .build();
        return EpisodioEntity.builder()
                .id(10L)
                .tipoAtencion(TipoAtencion.EMERGENCIA)
                .paciente(paciente)
                .estado(true)
                .build();
    }

    private UsuarioEntity usuario(Long id) {
        return UsuarioEntity.builder()
                .id(id)
                .username("farmacia")
                .persona(PersonaEntity.builder()
                        .id(31L)
                        .primerNombre("Carlos")
                        .primerApellido("Méndez")
                        .build())
                .build();
    }

    private motivoBajaEntity motivo(Long id) {
        motivoBajaEntity motivo = new motivoBajaEntity();
        motivo.setId(id);
        motivo.setNombre("Empaque dañado");
        motivo.setEstado(true);
        return motivo;
    }
}
