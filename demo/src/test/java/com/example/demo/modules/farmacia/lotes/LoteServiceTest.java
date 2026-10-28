package com.example.demo.modules.farmacia.lotes;

import com.example.demo.modules.farmacia.compras.CompraEntity;
import com.example.demo.modules.farmacia.compras.CompraRepository;
import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.insumosLog.insumosLogRepository;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorEntity;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private CompraEntity compraConProveedor(Long compraId, String codigo, Long proveedorId, String proveedorNombre) {
        ProveedorEntity proveedor = ProveedorEntity.builder()
                .id(proveedorId).codigo("PROV-" + proveedorId).nombre(proveedorNombre).estado(true).build();
        return CompraEntity.builder()
                .id(compraId).codigo(codigo).total(BigDecimal.ZERO).estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedor).build();
    }

    private viaAdminEntity viaAdminMock(Long id, String nombre) {
        viaAdminEntity via = new viaAdminEntity();
        via.setId(id);
        via.setNombre(nombre);
        via.setEstado(true);
        return via;
    }

    private unidadMedidaEntity unidadMedidaMock(String abreviatura) {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre("Miligramo");
        unidad.setAbreviatura(abreviatura);
        unidad.setEstado(true);
        return unidad;
    }

    private marcaEntity marcaMock(String nombre) {
        marcaEntity marca = new marcaEntity();
        marca.setId(1L);
        marca.setNombre(nombre);
        marca.setEstado(true);
        return marca;
    }

    private presentacionEntity presentacionMock(String nombre) {
        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(1L);
        presentacion.setNombre(nombre);
        presentacion.setEstado(true);
        return presentacion;
    }

    private medicamentoLogEntity medicamentoMock(
            Long id, String nombre, String codigo, String viaAdmin, Long viaAdminId,
            String presentacion, String marca, String unidad) {
        return medicamentoLogEntity.builder()
                .id(id).nombre(nombre).codigo(codigo).dosis(new BigDecimal("500")).estado(true)
                .unidadMedida(unidadMedidaMock(unidad))
                .marca(marcaMock(marca))
                .viaAdmin(viaAdminMock(viaAdminId, viaAdmin))
                .presentacion(presentacionMock(presentacion))
                .build();
    }

    private LoteEntity loteMedicamento(
            CompraEntity compra, Long id, String codigoLote, int cantidad, int disponible,
            LocalDate vencimiento, medicamentoLogEntity medicamento) {
        return LoteEntity.builder()
                .id(id).codigoLote(codigoLote).cantidad(cantidad).disponible(disponible).estado(true)
                .fechaVencimiento(vencimiento)
                .precioCompra(BigDecimal.ONE).precioVenta(BigDecimal.TEN).total(BigDecimal.TEN)
                .medicamentoLog(medicamento).compra(compra).build();
    }

    private LoteEntity loteInsumo(
            CompraEntity compra, Long id, String codigoLote, int cantidad, int disponible,
            LocalDate vencimiento, insumosLogEntity insumo) {
        return LoteEntity.builder()
                .id(id).codigoLote(codigoLote).cantidad(cantidad).disponible(disponible).estado(true)
                .fechaVencimiento(vencimiento)
                .precioCompra(BigDecimal.ONE).precioVenta(BigDecimal.TEN).total(BigDecimal.TEN)
                .insumoLog(insumo).compra(compra).build();
    }

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
    void actualizar_debePreservarUnidadesUsadasAlReducirCantidad() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(4)
                .fechaVencimiento(LocalDate.of(2027, 6, 30))
                .precioCompra(new BigDecimal("2.50")).total(new BigDecimal("25.00"))
                .compra(compra).insumoLog(insumo).estado(true).build();
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 7, new BigDecimal("2.50"));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L))
                .thenReturn(Optional.of(existente));
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of(existente));
        when(insumosLogRepository.findById(5L)).thenReturn(Optional.of(insumo));
        when(repository.save(any())).thenReturn(existente);
        when(repository.sumTotalesPorCompra(1L)).thenReturn(new BigDecimal("17.50"));
        when(compraRepository.save(any())).thenReturn(compra);
        when(mapper.toDTO(existente)).thenReturn(new LoteDTOs.Response(
                1L, "LT-001", 7, 1, true, LocalDate.of(2027, 6, 30),
                new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("17.50"),
                5L, "Gasas", null, null, 1L, "COM-1"));

        LoteDTOs.Response result = service.actualizar(1L, req);

        assertThat(existente.getCantidad()).isEqualTo(7);
        assertThat(existente.getDisponible()).isEqualTo(1);
        assertThat(result.disponible()).isEqualTo(1);
    }

    @Test
    void actualizar_debeRechazarReduccionMayorALaDisponible() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(4)
                .fechaVencimiento(LocalDate.of(2027, 6, 30))
                .precioCompra(new BigDecimal("2.50")).total(new BigDecimal("25.00"))
                .compra(compra).insumoLog(insumo).estado(true).build();
        LoteDTOs.Request req = requestInsumo(5L, "LT-001", LocalDate.of(2027, 6, 30), 5, new BigDecimal("2.50"));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(repository.findByCodigoLoteIgnoreCaseAndCompra_Id("LT-001", 1L))
                .thenReturn(Optional.of(existente));
        when(repository.findByCodigoLoteIgnoreCase("LT-001")).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.actualizar(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supera la cantidad disponible");

        verify(repository, never()).save(any());
    }

    @Test
    void eliminar_debeEliminarLoteSinEntregasYRecalcularCompra() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(10)
                .compra(compra).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.sumTotalesPorCompra(1L)).thenReturn(BigDecimal.ZERO);
        when(compraRepository.save(any())).thenReturn(compra);

        service.eliminar(1L);

        verify(repository).delete(existente);
        verify(compraRepository).save(compra);
    }

    @Test
    void eliminar_debeRechazarLoteConEntregas() {
        LoteEntity existente = LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(7)
                .compra(compra).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya se han entregado productos");

        verify(repository, never()).delete(any());
    }

    @Test
    void obtenerInventario_debeRetornarMedicamentosConDatosDeProductoCompraYProveedor() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Distribuidora la Paz");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity lote = loteMedicamento(
                compra, 10L, "Lt-2025", 50, 8, LocalDate.of(2028, 12, 10), amox);
        insumosLogEntity jeringa = insumosLogEntity.builder()
                .id(5L).nombre("Jeringa descartable").codigo("INS-005").detalle("5 ml")
                .estado(true).marca(marcaMock("Acme")).build();
        LoteEntity loteInsumo = loteInsumo(compra, 11L, "Tk-020", 20, 20, null, jeringa);

        when(repository.findByEstado(true)).thenReturn(List.of(lote, loteInsumo));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, null, null, null);

        assertThat(inventario).hasSize(1);
        LoteDTOs.InventarioResponse fila = inventario.get(0);
        assertThat(fila.tipo()).isEqualTo("MEDICAMENTO");
        assertThat(fila.idItem()).isEqualTo(9L);
        assertThat(fila.codigo()).isEqualTo("MED-009");
        assertThat(fila.nombre()).isEqualTo("Amoxicilina Genfar 500mg");
        assertThat(fila.descripcion()).isEqualTo("500 mg · Tabletas · Genfar");
        assertThat(fila.administracion()).isEqualTo("Oral");
        assertThat(fila.presentacion()).isEqualTo("Tabletas");
        assertThat(fila.cantidad()).isEqualTo(50);
        assertThat(fila.disponible()).isEqualTo(8);
        assertThat(fila.proveedorId()).isEqualTo(1L);
        assertThat(fila.proveedor()).isEqualTo("Distribuidora la Paz");
        assertThat(fila.codigoLote()).isEqualTo("Lt-2025");
        assertThat(fila.fechaVencimiento()).isEqualTo(LocalDate.of(2028, 12, 10));
        assertThat(fila.estado()).isTrue();
        assertThat(fila.idCompra()).isEqualTo(1L);
        assertThat(fila.codigoCompra()).isEqualTo("COM-1");
    }

    @Test
    void obtenerInventario_debeRetornarInsumosConCamposDeMedicamentoNulos() {
        CompraEntity compra = compraConProveedor(2L, "COM-2", 3L, "Insumos del Norte");
        insumosLogEntity jeringa = insumosLogEntity.builder()
                .id(5L).nombre("Jeringa descartable").codigo("INS-005").detalle("5 ml")
                .estado(true).marca(marcaMock("Acme")).build();
        LoteEntity lote = loteInsumo(compra, 11L, "Tk-020", 20, 12, null, jeringa);
        when(repository.findByEstado(true)).thenReturn(List.of(lote));

        LoteDTOs.InventarioResponse fila = service.obtenerInventario(
                "insumo", null, null, null, null, null, null).get(0);

        assertThat(fila.tipo()).isEqualTo("INSUMO");
        assertThat(fila.idItem()).isEqualTo(5L);
        assertThat(fila.codigo()).isEqualTo("INS-005");
        assertThat(fila.nombre()).isEqualTo("Jeringa descartable Acme 5 ml");
        assertThat(fila.descripcion()).isEqualTo("Acme · 5 ml");
        assertThat(fila.administracion()).isNull();
        assertThat(fila.presentacion()).isNull();
        assertThat(fila.fechaVencimiento()).isNull();
        assertThat(fila.disponible()).isEqualTo(12);
        assertThat(fila.proveedor()).isEqualTo("Insumos del Norte");
    }

    @Test
    void obtenerInventario_debeFiltrarPorNombreCodigoProveedorYViaAdmin() {
        CompraEntity compra1 = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        CompraEntity compra2 = compraConProveedor(2L, "COM-2", 2L, "Proveedor Dos");
        LoteEntity amox = loteMedicamento(compra1, 10L, "LT-A", 50, 8, LocalDate.of(2028, 12, 10),
                medicamentoMock(9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg"));
        LoteEntity ibu = loteMedicamento(compra2, 20L, "LT-B", 30, 10, LocalDate.of(2028, 12, 10),
                medicamentoMock(8L, "Ibuprofeno", "MED-008", "Intravenosa", 3L, "Cápsulas", "Bayer", "mg"));
        when(repository.findByEstado(true)).thenReturn(List.of(amox, ibu));

        assertThat(service.obtenerInventario("medicamento", "amox", null, null, null, null, null))
                .hasSize(1);
        assertThat(service.obtenerInventario("medicamento", "MED-008", null, null, null, null, null))
                .hasSize(1);
        assertThat(service.obtenerInventario("medicamento", null, 1L, null, null, null, null))
                .hasSize(1);
        assertThat(service.obtenerInventario("medicamento", null, null, 3L, null, null, null))
                .extracting(LoteDTOs.InventarioResponse::nombre)
                .containsExactly("Ibuprofeno Bayer 500mg");
    }

    @Test
    void obtenerInventario_vencidosDebeIncluirSoloLotesConStock() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity conStock = loteMedicamento(compra, 10L, "LT-A", 50, 5,
                LocalDate.now().minusDays(1), amox);
        LoteEntity sinStock = loteMedicamento(compra, 11L, "LT-B", 50, 0,
                LocalDate.now().minusDays(1), amox);
        when(repository.findByEstado(true)).thenReturn(List.of(conStock, sinStock));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "vencidos", null, null);

        assertThat(inventario).extracting(LoteDTOs.InventarioResponse::id).containsExactly(10L);
    }

    @Test
    void obtenerInventario_porVencerDebeUsarTreintaDiasPorDefecto() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity enRango = loteMedicamento(compra, 10L, "LT-A", 50, 5,
                LocalDate.now().plusDays(30), amox);
        LoteEntity fueraRango = loteMedicamento(compra, 11L, "LT-B", 50, 5,
                LocalDate.now().plusDays(31), amox);
        when(repository.findByEstado(true)).thenReturn(List.of(enRango, fueraRango));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "por-vencer", null, null);

        assertThat(inventario).extracting(LoteDTOs.InventarioResponse::id).containsExactly(10L);
    }

    @Test
    void obtenerInventario_stockBajoDebeSumarTodoElStockDelProducto() {
        CompraEntity compra1 = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        CompraEntity compra2 = compraConProveedor(2L, "COM-2", 2L, "Proveedor Dos");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        medicamentoLogEntity ibu = medicamentoMock(
                8L, "Ibuprofeno", "MED-008", "Intravenosa", 3L, "Cápsulas", "Bayer", "mg");
        LoteEntity amoxProveedorUno = loteMedicamento(compra1, 10L, "LT-A", 50, 9, null, amox);
        LoteEntity amoxProveedorDos = loteMedicamento(compra2, 11L, "LT-B", 50, 10, null, amox);
        LoteEntity ibuProveedorUno = loteMedicamento(compra1, 20L, "LT-C", 50, 10, null, ibu);
        LoteEntity ibuProveedorDos = loteMedicamento(compra2, 21L, "LT-D", 50, 10, null, ibu);

        when(repository.findByEstado(true)).thenReturn(List.of(
                amoxProveedorUno, amoxProveedorDos, ibuProveedorUno, ibuProveedorDos));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "stock-bajo", null, null);

        // Amoxicilina suma 19 (< 20) y se incluye; Ibuprofeno suma 20 y no es stock bajo.
        assertThat(inventario).extracting(LoteDTOs.InventarioResponse::id)
                .containsExactly(10L, 11L);

        assertThat(service.obtenerInventario(
                "medicamento", null, 2L, null, "stock-bajo", null, null))
                .extracting(LoteDTOs.InventarioResponse::id)
                .containsExactly(11L);
    }

    @Test
    void obtenerInventario_stockBajoNoDebeSumarLotesVencidos() {
        CompraEntity compra1 = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        CompraEntity compra2 = compraConProveedor(2L, "COM-2", 2L, "Proveedor Dos");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity vencido = loteMedicamento(compra1, 10L, "LT-A", 50, 15,
                LocalDate.now().minusDays(1), amox);
        LoteEntity vigente = loteMedicamento(compra2, 11L, "LT-B", 50, 10,
                LocalDate.now().plusMonths(6), amox);
        when(repository.findByEstado(true)).thenReturn(List.of(vencido, vigente));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "stock-bajo", null, null);

        // Si sumara el lote vencido el total sería 25; al excluirlo queda en 10 y sí es stock bajo.
        assertThat(inventario).extracting(LoteDTOs.InventarioResponse::id).containsExactly(11L);
    }

    @Test
    void obtenerInventario_sinStockDebeSumarTodosLosLotesDelMedicamento() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        medicamentoLogEntity ibu = medicamentoMock(
                8L, "Ibuprofeno", "MED-008", "Intravenosa", 3L, "Cápsulas", "Bayer", "mg");
        LoteEntity amoxSinStock = loteMedicamento(compra, 10L, "LT-A", 50, 0, null, amox);
        LoteEntity amoxConStock = loteMedicamento(compra, 11L, "LT-B", 50, 5, null, amox);
        LoteEntity ibuSinStockA = loteMedicamento(compra, 20L, "LT-C", 30, 0,
                LocalDate.now().plusMonths(6), ibu);
        LoteEntity ibuSinStockB = loteMedicamento(compra, 21L, "LT-D", 30, 0,
                LocalDate.now().plusMonths(3), ibu);

        when(repository.findByEstado(true))
                .thenReturn(List.of(amoxSinStock, amoxConStock, ibuSinStockA, ibuSinStockB));
        when(medicamentoLogRepository.findByEstado(true)).thenReturn(List.of(amox, ibu));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "sin-stock", null, null);

        assertThat(inventario).hasSize(1);
        assertThat(inventario.get(0).idItem()).isEqualTo(8L);
        assertThat(inventario.get(0).nombre()).isEqualTo("Ibuprofeno Bayer 500mg");
        assertThat(inventario.get(0).disponible()).isZero();
        assertThat(inventario.get(0).id()).isEqualTo(21L);
    }

    @Test
    void obtenerInventario_sinStockDebeIncluirMedicamentosSinLotes() {
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");

        when(repository.findByEstado(true)).thenReturn(List.of());
        when(medicamentoLogRepository.findByEstado(true)).thenReturn(List.of(amox));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "sin-stock", null, null);

        assertThat(inventario).hasSize(1);
        LoteDTOs.InventarioResponse fila = inventario.get(0);
        assertThat(fila.id()).isNull();
        assertThat(fila.idItem()).isEqualTo(9L);
        assertThat(fila.codigo()).isEqualTo("MED-009");
        assertThat(fila.nombre()).isEqualTo("Amoxicilina Genfar 500mg");
        assertThat(fila.cantidad()).isZero();
        assertThat(fila.disponible()).isZero();
        assertThat(fila.codigoLote()).isNull();
        assertThat(fila.proveedor()).isNull();

        assertThat(service.obtenerInventario(
                "medicamento", null, 1L, null, "sin-stock", null, null)).isEmpty();
    }

    @Test
    void obtenerInventario_eliminadosDebeForzarEstadoFalse() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity eliminado = loteMedicamento(compra, 10L, "LT-A", 50, 5, null, amox);
        eliminado.setEstado(false);
        when(repository.findByEstado(false)).thenReturn(List.of(eliminado));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, "eliminados", true, null);

        assertThat(inventario).hasSize(1);
        assertThat(inventario.get(0).estado()).isFalse();
        verify(repository).findByEstado(false);
    }

    @Test
    void obtenerInventario_debeOrdenarPorVencimientoYDejarSinFechaAlFinal() {
        CompraEntity compra = compraConProveedor(1L, "COM-1", 1L, "Proveedor Uno");
        medicamentoLogEntity amox = medicamentoMock(
                9L, "Amoxicilina", "MED-009", "Oral", 2L, "Tabletas", "Genfar", "mg");
        LoteEntity sinFecha = loteMedicamento(compra, 10L, "LT-A", 50, 5, null, amox);
        LoteEntity lejano = loteMedicamento(compra, 11L, "LT-B", 50, 5,
                LocalDate.now().plusYears(2), amox);
        LoteEntity cercano = loteMedicamento(compra, 12L, "LT-C", 50, 5,
                LocalDate.now().plusYears(1), amox);
        when(repository.findByEstado(true)).thenReturn(List.of(sinFecha, lejano, cercano));

        List<LoteDTOs.InventarioResponse> inventario = service.obtenerInventario(
                "medicamento", null, null, null, null, null, null);

        assertThat(inventario).extracting(LoteDTOs.InventarioResponse::id)
                .containsExactly(12L, 11L, 10L);
    }

    @Test
    void obtenerInventario_debeRechazarTipoEstadoODiasInvalidos() {
        assertThatThrownBy(() -> service.obtenerInventario(
                "otro", null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");

        assertThatThrownBy(() -> service.obtenerInventario(
                "medicamento", null, null, null, "desconocido", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estado de inventario");

        assertThatThrownBy(() -> service.obtenerInventario(
                "medicamento", null, null, null, null, null, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativos");

        verify(repository, never()).findByEstado(anyBoolean());
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
