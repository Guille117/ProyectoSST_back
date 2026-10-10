package com.example.demo.modules.farmacia.lotes;

import com.example.demo.modules.farmacia.compras.CompraEntity;
import com.example.demo.modules.farmacia.compras.CompraRepository;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository repository;

    @Mock
    private LoteMapper mapper;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private insumosLogRepository insumosLogRepository;

    @Mock
    private medicamentoLogRepository medicamentoLogRepository;

    @InjectMocks
    private LoteService service;

    private final CompraEntity compra = CompraEntity.builder().id(1L).codigo("COM-1").total(BigDecimal.ZERO).estado(true).build();
    private final insumosLogEntity insumo = insumosLogEntity.builder().id(5L).nombre("Gasas").estado(true).build();

    private LoteDTOs.Request requestInsumo(Long idInsumo, String codigo, LocalDate fecha, Integer cantidad, BigDecimal precioCompra) {
        return new LoteDTOs.Request(1L, idInsumo, null, codigo, cantidad, fecha, precioCompra, precioCompra.multiply(BigDecimal.valueOf(2)));
    }

    @Test
    void crear_debeGuardarConDisponibleYTotalCalculados() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 10, new BigDecimal("2.50"));

        LoteEntity entity = new LoteEntity();
        LoteEntity guardado = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(10).estado(true)
                .fechaVencimiento(LocalDate.of(2027, 6, 30))
                .precioCompra(new BigDecimal("2.50")).precioVenta(new BigDecimal("5.00"))
                .total(new BigDecimal("25.00")).compra(compra).insumoLog(insumo).build();
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("25.00"),
                5L, "Gasas", null, null, 1L, "COM-1");

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L)).thenReturn(Optional.empty());
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of());
        when(insumosLogRepository.findById(5L)).thenReturn(Optional.of(insumo));
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(repository.sumTotalesPorCompra(1L)).thenReturn(new BigDecimal("25.00"));
        when(compraRepository.save(any())).thenReturn(compra);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        LoteDTOs.Response result = service.crear(req);

        assertThat(result.disponible()).isEqualTo(10);
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(compra.getTotal()).isEqualByComparingTo(new BigDecimal("25.00"));
        verify(repository).save(any());
        verify(compraRepository).save(compra);
    }

    @Test
    void crear_debeRechazarCuandoAmbosItemVienen() {
        LoteDTOs.Request req = new LoteDTOs.Request(1L, 5L, 9L, "LT-001", 10,
                LocalDate.of(2027, 6, 30), BigDecimal.ONE, BigDecimal.TEN);

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactamente uno");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarCuandoNingunItemViene() {
        LoteDTOs.Request req = new LoteDTOs.Request(1L, null, null, "LT-001", 10,
                LocalDate.of(2027, 6, 30), BigDecimal.ONE, BigDecimal.TEN);

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactamente uno");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarCodigoDuplicadoEnMismaCompra() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 10, BigDecimal.ONE);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L))
                .thenReturn(Optional.of(LoteEntity.builder().id(99L).codigoLote("LT-001").build()));

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un lote con el código LT-001 en esta compra");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarFechaVencimientoInconsistente() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 10, BigDecimal.ONE);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L)).thenReturn(Optional.empty());
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of(
                LoteEntity.builder().id(50L).codigoLote("LT-001").fechaVencimiento(LocalDate.of(2026, 1, 1)).build()
        ));

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de vencimiento debe ser la misma");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarCompraInexistente() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 10, BigDecimal.ONE);
        when(compraRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Compra no encontrada");
    }

    @Test
    void crear_debeRechazarInsumoInexistente() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 10, BigDecimal.ONE);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L)).thenReturn(Optional.empty());
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of());
        when(mapper.toEntity(req)).thenReturn(new LoteEntity());
        when(insumosLogRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo no encontrado");
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        LoteEntity entity = LoteEntity.builder().id(1L).codigoLote("LT-001").compra(compra).build();
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, LocalDate.of(2027, 6, 30),
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN,
                null, null, null, null, 1L, "COM-1");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lote no encontrado");
    }

    @Test
    void actualizar_debeRecalcularDisponibleTotalYCompra() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(10)
                .fechaVencimiento(LocalDate.of(2027, 6, 30))
                .precioCompra(new BigDecimal("2.50")).total(new BigDecimal("25.00"))
                .compra(compra).insumoLog(insumo).estado(true).build();
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 20, new BigDecimal("3.00"));

        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 20, 20, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("3.00"), new BigDecimal("6.00"), new BigDecimal("60.00"),
                5L, "Gasas", null, null, 1L, "COM-1");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L))
                .thenReturn(Optional.of(existente));
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of(existente));
        when(insumosLogRepository.findById(5L)).thenReturn(Optional.of(insumo));
        when(repository.save(any())).thenReturn(existente);
        when(repository.sumTotalesPorCompra(1L)).thenReturn(new BigDecimal("60.00"));
        when(compraRepository.save(any())).thenReturn(compra);
        when(mapper.toDTO(existente)).thenReturn(resp);

        LoteDTOs.Response result = service.actualizar(1L, req);

        assertThat(result.disponible()).isEqualTo(20);
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(compra.getTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void actualizar_debeBloquearCambioDeFechaSiOtroLoteMismoCodigoExiste() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10)
                .fechaVencimiento(LocalDate.of(2027, 6, 30))
                .precioCompra(BigDecimal.ONE).total(BigDecimal.TEN)
                .compra(compra).insumoLog(insumo).estado(true).build();
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2028, 12, 31), 10, BigDecimal.ONE);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L))
                .thenReturn(Optional.of(existente));
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of(
                existente,
                LoteEntity.builder().id(77L).codigoLote("LT-001").fechaVencimiento(LocalDate.of(2027, 6, 30)).build()
        ));

        assertThatThrownBy(() -> service.actualizar(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de vencimiento debe ser la misma");

        verify(repository, never()).save(any());
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        LoteEntity entity = LoteEntity.builder().id(1L).codigoLote("LT-001").estado(true).compra(compra).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void buscarPorCodigo_retornaVacioSiEstaEnBlanco() {
        assertThat(service.buscarPorCodigo(" ", null)).isEmpty();
        verify(repository, never()).findByCodigoLoteContainingIgnoreCase(any());
    }

    @Test
    void crear_debeFuncionarConMedicamento() {
        medicamentoLogEntity medicamento = medicamentoLogEntity.builder().id(9L).nombre("Ibuprofeno").build();
        LoteDTOs.Request req = new LoteDTOs.Request(1L, null, 9L, "LT-MED", 5,
                LocalDate.of(2027, 1, 1), new BigDecimal("4.00"), new BigDecimal("8.00"));

        LoteEntity entity = new LoteEntity();
        LoteEntity guardado = LoteEntity.builder().id(2L).codigoLote("LT-MED").cantidad(5).disponible(5)
                .estado(true).fechaVencimiento(LocalDate.of(2027, 1, 1))
                .precioCompra(new BigDecimal("4.00")).precioVenta(new BigDecimal("8.00"))
                .total(new BigDecimal("20.00")).compra(compra).medicamentoLog(medicamento).build();
        LoteDTOs.Response resp = new LoteDTOs.Response(
                2L, "LT-MED", 5, 5, true, LocalDate.of(2027, 1, 1),
                new BigDecimal("4.00"), new BigDecimal("8.00"), new BigDecimal("20.00"),
                null, null, 9L, "Ibuprofeno", 1L, "COM-1");

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-MED", 1L)).thenReturn(Optional.empty());
        when(repository.findByCodigoLoteIgnoreCase("LT-MED")).thenReturn(List.of());
        when(medicamentoLogRepository.findById(9L)).thenReturn(Optional.of(medicamento));
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(repository.sumTotalesPorCompra(1L)).thenReturn(new BigDecimal("20.00"));
        when(compraRepository.save(any())).thenReturn(compra);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        LoteDTOs.Response result = service.crear(req);

        assertThat(result.idMedicamentoLog()).isEqualTo(9L);
        assertThat(result.idInsumoLog()).isNull();
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void crear_debePermitirInsumoSinFechaVencimiento() {
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", null, 10, BigDecimal.ONE);

        LoteEntity entity = new LoteEntity();
        LoteEntity guardado = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(10).estado(true)
                .precioCompra(BigDecimal.ONE).precioVenta(BigDecimal.TEN)
                .total(BigDecimal.TEN).compra(compra).insumoLog(insumo).build();
        LoteDTOs.Response resp = new LoteDTOs.Response(
                1L, "LT-001", 10, 10, true, null,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN,
                5L, "Gasas", null, null, 1L, "COM-1");

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L)).thenReturn(Optional.empty());
        when(insumosLogRepository.findById(5L)).thenReturn(Optional.of(insumo));
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any())).thenReturn(guardado);
        when(repository.sumTotalesPorCompra(1L)).thenReturn(BigDecimal.TEN);
        when(compraRepository.save(any())).thenReturn(compra);
        when(mapper.toDTO(guardado)).thenReturn(resp);

        LoteDTOs.Response result = service.crear(req);

        assertThat(result.fechaVencimiento()).isNull();
        verify(repository).save(argThat(lote -> lote.getFechaVencimiento() == null));
        verify(repository, never()).findByCodigoLoteIgnoreCase(any());
    }

    @Test
    void crear_debeRechazarMedicamentoSinFechaVencimiento() {
        LoteDTOs.Request req = new LoteDTOs.Request(1L, null, 9L, "LT-MED", 5, null,
                new BigDecimal("4.00"), new BigDecimal("8.00"));

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de vencimiento es obligatoria para medicamentos");

        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeRechazarMedicamentoSinFechaVencimiento() {
        LoteDTOs.Request req = new LoteDTOs.Request(1L, null, 9L, "LT-MED", 5, null,
                new BigDecimal("4.00"), new BigDecimal("8.00"));

        assertThatThrownBy(() -> service.actualizar(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de vencimiento es obligatoria para medicamentos");

        verify(repository, never()).save(any());
    }
}
