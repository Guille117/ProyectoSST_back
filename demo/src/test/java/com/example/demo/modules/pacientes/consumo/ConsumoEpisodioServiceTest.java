package com.example.demo.modules.pacientes.consumo;

import com.example.demo.modules.clinica.alta.EstadoAlta;
import com.example.demo.modules.clinica.alta.altaEntity;
import com.example.demo.modules.clinica.alta.altaRepository;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.salidas.EstadoDetalleSalida;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaDetalleEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoEntity;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaRepository;
import com.example.demo.modules.farmacia.salidas.TipoMovimientoSalida;
import com.example.demo.modules.pacientes.area.areaEntity;
import com.example.demo.modules.pacientes.camas.camasEntity;
import com.example.demo.modules.pacientes.camas.camasRepository;
import com.example.demo.modules.pacientes.habitaciones.habitacionesEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteEntity;
import com.example.demo.modules.pacientes.tipoCama.tipoCamaEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumoEpisodioServiceTest {

    private static final Long EPISODIO_ID = 7L;
    private static final Long PACIENTE_ID = 3L;
    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 10, 20, 8, 0);

    @Mock
    private EpisodioRepository episodioRepository;
    @Mock
    private camasRepository camasRepository;
    @Mock
    private altaRepository altaRepository;
    @Mock
    private SolicitudSalidaRepository solicitudSalidaRepository;
    @Mock
    private SolicitudSalidaMovimientoRepository movimientoRepository;

    @InjectMocks
    private ConsumoEpisodioService service;

    @ParameterizedTest(name = "{0} horas de estancia => {1} día(s)")
    @CsvSource({
            "2, 1",
            "24, 1",
            "25, 2",
            "26, 2",
            "48, 2",
            "49, 3"
    })
    void calcularConsumo_camaCobraDesdeMasDeUnaHora(long horas, int diasEsperados) {
        when(episodioRepository.findById(EPISODIO_ID)).thenReturn(Optional.of(episodio()));
        when(camasRepository.findByPaciente_Id(PACIENTE_ID)).thenReturn(Optional.of(cama()));
        when(altaRepository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                EPISODIO_ID, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(alta(INGRESO.plusHours(horas))));
        when(solicitudSalidaRepository.findByEpisodio_IdOrderByFechaSolicitudAsc(EPISODIO_ID))
                .thenReturn(List.of());

        ConsumoEpisodioDTOs.Response respuesta = service.obtenerConsumo(EPISODIO_ID);

        assertEquals(1, respuesta.items().size());
        ConsumoEpisodioDTOs.ItemResponse cama = respuesta.items().get(0);
        assertEquals("Cama", cama.tipo());
        assertEquals(diasEsperados, cama.cantidad());
        assertEquals(diasEsperados == 1 ? "día" : "días", cama.unidad());
        assertEquals(new BigDecimal("250.00"), cama.precioUnitario());
        assertEquals(new BigDecimal("250.00").multiply(BigDecimal.valueOf(diasEsperados)),
                cama.subtotal());
    }

    @Test
    void calcularConsumo_noCobraCamaConUnaHoraOMenos() {
        when(episodioRepository.findById(EPISODIO_ID)).thenReturn(Optional.of(episodio()));
        when(camasRepository.findByPaciente_Id(PACIENTE_ID)).thenReturn(Optional.of(cama()));
        when(altaRepository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                EPISODIO_ID, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(alta(INGRESO.plusHours(1))));
        when(solicitudSalidaRepository.findByEpisodio_IdOrderByFechaSolicitudAsc(EPISODIO_ID))
                .thenReturn(List.of());

        ConsumoEpisodioDTOs.Response respuesta = service.obtenerConsumo(EPISODIO_ID);

        assertEquals(0, respuesta.items().size());
        assertEquals(new BigDecimal("0.00"), respuesta.total());
    }

    @Test
    void obtenerConsumo_usaCamaYPrecioDelEpisodioAunqueYaNoEsteVinculada() {
        EpisodioEntity episodio = episodio();
        camasEntity cama = cama();
        episodio.setCama(cama);
        episodio.setPrecioCama(new BigDecimal("300.00"));

        when(episodioRepository.findById(EPISODIO_ID)).thenReturn(Optional.of(episodio));
        when(altaRepository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                EPISODIO_ID, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(alta(INGRESO.plusHours(2))));
        when(solicitudSalidaRepository.findByEpisodio_IdOrderByFechaSolicitudAsc(EPISODIO_ID))
                .thenReturn(List.of());

        ConsumoEpisodioDTOs.Response respuesta = service.obtenerConsumo(EPISODIO_ID);

        assertEquals(1, respuesta.items().size());
        ConsumoEpisodioDTOs.ItemResponse camaItem = respuesta.items().get(0);
        assertEquals("Cama", camaItem.tipo());
        assertEquals(1, camaItem.cantidad());
        assertEquals(new BigDecimal("300.00"), camaItem.precioUnitario());
        assertEquals(new BigDecimal("300.00"), camaItem.subtotal());
    }

    @Test
    void obtenerConsumo_descuentaDevolucionesYSumaLaCama() {
        SolicitudSalidaDetalleEntity detalle = detalle();
        SolicitudSalidaEntity solicitud = solicitud(detalle);
        LoteEntity lote = LoteEntity.builder()
                .id(50L)
                .precioVenta(new BigDecimal("1.50"))
                .build();

        when(episodioRepository.findById(EPISODIO_ID)).thenReturn(Optional.of(episodio()));
        when(camasRepository.findByPaciente_Id(PACIENTE_ID)).thenReturn(Optional.of(cama()));
        when(altaRepository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                EPISODIO_ID, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(alta(INGRESO.plusHours(25))));
        when(solicitudSalidaRepository.findByEpisodio_IdOrderByFechaSolicitudAsc(EPISODIO_ID))
                .thenReturn(List.of(solicitud));
        when(movimientoRepository.findByDetalle_Solicitud_IdOrderByFechaHoraAsc(20L))
                .thenReturn(List.of(
                        movimiento(detalle, lote, TipoMovimientoSalida.ENTREGA, 10),
                        movimiento(detalle, lote, TipoMovimientoSalida.DEVOLUCION, 2)));

        ConsumoEpisodioDTOs.Response respuesta = service.obtenerConsumo(EPISODIO_ID);

        assertEquals(2, respuesta.items().size());
        ConsumoEpisodioDTOs.ItemResponse medicamento = respuesta.items().stream()
                .filter(item -> item.tipo().equals("Medicamento"))
                .findFirst()
                .orElseThrow();
        assertEquals("Paracetamol", medicamento.nombre());
        assertEquals(8, medicamento.cantidad());
        assertEquals(new BigDecimal("1.50"), medicamento.precioUnitario());
        assertEquals(new BigDecimal("12.00"), medicamento.subtotal());

        ConsumoEpisodioDTOs.ItemResponse cama = respuesta.items().stream()
                .filter(item -> item.tipo().equals("Cama"))
                .findFirst()
                .orElseThrow();
        assertEquals(2, cama.cantidad());
        assertEquals(new BigDecimal("500.00"), cama.subtotal());
        assertEquals(new BigDecimal("512.00"), respuesta.total());
    }

    @Test
    void obtenerConsumo_rechazaEpisodioInexistente() {
        when(episodioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.obtenerConsumo(99L));
    }

    private EpisodioEntity episodio() {
        return EpisodioEntity.builder()
                .id(EPISODIO_ID)
                .fechaHoraIngreso(INGRESO)
                .paciente(PacienteEntity.builder().id(PACIENTE_ID).build())
                .build();
    }

    private camasEntity cama() {
        habitacionesEntity habitacion = new habitacionesEntity();
        habitacion.setNombre("101");
        areaEntity area = new areaEntity();
        area.setNombre("Hospitalización");
        tipoCamaEntity tipo = new tipoCamaEntity();
        tipo.setNombre("Individual");

        return camasEntity.builder()
                .id(11L)
                .codigo("A-106")
                .precio(new BigDecimal("250.00"))
                .habitacion(habitacion)
                .area(area)
                .tipo(tipo)
                .build();
    }

    private altaEntity alta(LocalDateTime egreso) {
        return altaEntity.builder()
                .fechaHoraEgreso(egreso)
                .build();
    }

    private SolicitudSalidaDetalleEntity detalle() {
        return SolicitudSalidaDetalleEntity.builder()
                .id(30L)
                .tipo(TipoRecursoProcedimiento.MEDICAMENTO)
                .referenciaId(4L)
                .nombre("Paracetamol")
                .cantidadSolicitada(10)
                .cantidadEntregada(10)
                .estado(EstadoDetalleSalida.COMPLETADA)
                .build();
    }

    private SolicitudSalidaEntity solicitud(SolicitudSalidaDetalleEntity detalle) {
        SolicitudSalidaEntity solicitud = SolicitudSalidaEntity.builder()
                .id(20L)
                .episodio(episodio())
                .detalles(new ArrayList<>())
                .build();
        solicitud.agregarDetalle(detalle);
        return solicitud;
    }

    private SolicitudSalidaMovimientoEntity movimiento(
            SolicitudSalidaDetalleEntity detalle,
            LoteEntity lote,
            TipoMovimientoSalida tipo,
            int cantidad) {
        return SolicitudSalidaMovimientoEntity.builder()
                .detalle(detalle)
                .lote(lote)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .build();
    }
}
