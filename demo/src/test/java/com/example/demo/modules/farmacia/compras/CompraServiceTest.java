package com.example.demo.modules.farmacia.compras;

import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.lotes.LoteDTOs;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.lotes.LoteService;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorEntity;
import com.example.demo.modules.farmacia.proveedores.ProveedorRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository repository;

    @Mock
    private CompraMapper mapper;

    @Mock
    private ComprobanteArchivoStorage comprobanteStorage;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private LoteService loteService;

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private CompraService service;

    private final MockMultipartFile comprobante = new MockMultipartFile(
            "comprobante", "factura.pdf", "application/pdf", "%PDF-1.4 contenido".getBytes());

    private ProveedorEntity proveedorActivo() {
        return ProveedorEntity.builder().id(1L).codigo("PROV-01").nombre("Proveedor SA").estado(true).build();
    }

    private CompraDTOs.LoteEntrada loteMedicamento(Long idItem, String codigo) {
        return new CompraDTOs.LoteEntrada(true, idItem, codigo, 10,
                LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.valueOf(15));
    }

    private CompraDTOs.LoteEntrada loteInsumo(Long idItem, String codigo) {
        return new CompraDTOs.LoteEntrada(false, idItem, codigo, 5,
                LocalDate.now().plusMonths(6), BigDecimal.valueOf(8), BigDecimal.valueOf(12));
    }

    private CompraDTOs.Request requestConLotes(List<CompraDTOs.LoteEntrada> lotes) {
        return new CompraDTOs.Request(1L, lotes);
    }

    private LoteDTOs.Response respuestaLoteMock(String codigo) {
        return new LoteDTOs.Response(1L, codigo, 10, 10, true, LocalDate.now().plusMonths(6),
                BigDecimal.TEN, BigDecimal.valueOf(15), BigDecimal.valueOf(100),
                null, null, 3L, "Amoxicilina", 1L, "COM-1");
    }

    @Test
    void crear_debeGuardarCompraYLotesCuandoDatosSonValidos() {
        ProveedorEntity proveedor = proveedorActivo();
        List<CompraDTOs.LoteEntrada> lotes = List.of(
                loteMedicamento(3L, "LOTE-A"),
                loteInsumo(7L, "LOTE-B"));
        CompraDTOs.Request req = requestConLotes(lotes);
        CompraEntity guardada = CompraEntity.builder()
                .id(1L).codigo("COM-1").total(BigDecimal.valueOf(150))
                .comprobante("uploads/comprobantes/compra-1-abc.pdf").estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedor).build();
        CompraDTOs.Response base = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.valueOf(150), "uploads/comprobantes/compra-1-abc.pdf", true,
                1L, "Proveedor SA", guardada.getFecha(), null);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
        when(comprobanteStorage.guardar(any(), anyLong())).thenReturn("uploads/comprobantes/compra-1-abc.pdf");
        when(mapper.toDTO(any())).thenReturn(base);
        when(loteService.crear(any())).thenReturn(respuestaLoteMock("LOTE-A"));

        CompraDTOs.Response result = service.crear(comprobante, req);

        assertThat(result.codigo()).isEqualTo("COM-1");
        assertThat(result.lotes()).hasSize(2);
        verify(loteService).crear(argThat(r ->
                r.idCompra().equals(1L)
                        && r.idMedicamentoLog() != null
                        && r.idMedicamentoLog().equals(3L)
                        && r.idInsumoLog() == null
                        && r.codigoLote().equals("LOTE-A")));
        verify(loteService).crear(argThat(r ->
                r.idCompra().equals(1L)
                        && r.idInsumoLog() != null
                        && r.idInsumoLog().equals(7L)
                        && r.idMedicamentoLog() == null
                        && r.codigoLote().equals("LOTE-B")));
        verify(repository, times(2)).save(any());
        verify(comprobanteStorage, never()).eliminar(any());
    }

    @Test
    void crear_debeGuardarSinComprobante() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.Request req = requestConLotes(List.of(loteMedicamento(3L, "LOTE-A")));
        CompraDTOs.Response base = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, null, true,
                1L, "Proveedor SA", LocalDateTime.now(), null);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
        when(mapper.toDTO(any())).thenReturn(base);
        when(loteService.crear(any())).thenReturn(respuestaLoteMock("LOTE-A"));

        CompraDTOs.Response result = service.crear(null, req);

        assertThat(result.comprobante()).isNull();
        assertThat(result.lotes()).hasSize(1);
        verify(repository, times(2)).save(argThat(e -> e.getComprobante() == null));
        verify(comprobanteStorage, never()).guardar(any(), anyLong());
        verify(comprobanteStorage, never()).eliminar(any());
        verify(loteService).crear(any());
    }

    @Test
    void crear_debeIgnorarComprobanteVacio() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.Request req = requestConLotes(List.of(loteMedicamento(3L, "LOTE-A")));
        CompraDTOs.Response base = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, null, true,
                1L, "Proveedor SA", LocalDateTime.now(), null);
        MockMultipartFile vacio = new MockMultipartFile(
                "comprobante", "vacio.pdf", "application/pdf", new byte[0]);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
        when(mapper.toDTO(any())).thenReturn(base);
        when(loteService.crear(any())).thenReturn(respuestaLoteMock("LOTE-A"));

        CompraDTOs.Response result = service.crear(vacio, req);

        assertThat(result.comprobante()).isNull();
        verify(repository, times(2)).save(argThat(e -> e.getComprobante() == null));
        verify(comprobanteStorage, never()).guardar(any(), anyLong());
        verify(loteService).crear(any());
    }

    @Test
    void crear_debeRechazarSinProveedor() {
        CompraDTOs.Request req = new CompraDTOs.Request(null, List.of(loteMedicamento(3L, "LOTE-A")));

        assertThatThrownBy(() -> service.crear(comprobante, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("proveedor es obligatorio");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarProveedorInexistente() {
        CompraDTOs.Request req = new CompraDTOs.Request(99L, List.of(loteMedicamento(3L, "LOTE-A")));
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(comprobante, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Proveedor no encontrado");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarProveedorInactivo() {
        ProveedorEntity inactivo = ProveedorEntity.builder().id(2L).codigo("PROV-02").nombre("Inactivo").estado(false).build();
        CompraDTOs.Request req = new CompraDTOs.Request(2L, List.of(loteMedicamento(3L, "LOTE-A")));
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> service.crear(comprobante, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactivo");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarSinLotes() {
        ProveedorEntity proveedor = proveedorActivo();
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        assertThatThrownBy(() -> service.crear(comprobante, requestConLotes(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un lote");

        assertThatThrownBy(() -> service.crear(comprobante, requestConLotes(List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un lote");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarLoteSinIdItem() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.LoteEntrada sinItem = new CompraDTOs.LoteEntrada(true, null, "LOTE-A", 10,
                LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.valueOf(15));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        assertThatThrownBy(() -> service.crear(comprobante, requestConLotes(List.of(sinItem))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id del item");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarLotesConCodigoDuplicado() {
        ProveedorEntity proveedor = proveedorActivo();
        List<CompraDTOs.LoteEntrada> lotes = List.of(
                loteMedicamento(3L, "LOTE-A"),
                loteInsumo(7L, "lote-a"));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        assertThatThrownBy(() -> service.crear(comprobante, requestConLotes(lotes)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicado");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debePermitirLoteDeInsumoSinFechaVencimiento() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.LoteEntrada insumoSinFecha = new CompraDTOs.LoteEntrada(false, 7L, "LOTE-B", 5,
                null, BigDecimal.valueOf(8), BigDecimal.valueOf(12));
        CompraDTOs.Response base = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, null, true,
                1L, "Proveedor SA", LocalDateTime.now(), null);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
        when(mapper.toDTO(any())).thenReturn(base);

        CompraDTOs.Response result = service.crear(null, requestConLotes(List.of(insumoSinFecha)));

        assertThat(result.id()).isEqualTo(1L);
        verify(loteService).crear(argThat(r ->
                r.idInsumoLog() != null
                        && r.idInsumoLog().equals(7L)
                        && r.idMedicamentoLog() == null
                        && r.fechaVencimiento() == null));
    }

    @Test
    void crear_debeRechazarLoteDeMedicamentoSinFechaVencimiento() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.LoteEntrada medicamentoSinFecha = new CompraDTOs.LoteEntrada(true, 3L, "LOTE-A", 10,
                null, BigDecimal.TEN, BigDecimal.valueOf(15));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        assertThatThrownBy(() -> service.crear(comprobante, requestConLotes(List.of(medicamentoSinFecha))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de vencimiento del lote es obligatoria para medicamentos");

        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeEliminarArchivoSiSaveFalla() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraDTOs.Request req = requestConLotes(List.of(loteMedicamento(3L, "LOTE-A")));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
                return e;
            }
            throw new RuntimeException("fallo de BD");
        });
        when(comprobanteStorage.guardar(any(), anyLong())).thenReturn("uploads/comprobantes/compra-1-abc.pdf");

        assertThatThrownBy(() -> service.crear(comprobante, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("fallo de BD");

        verify(comprobanteStorage).eliminar("uploads/comprobantes/compra-1-abc.pdf");
    }

    @Test
    void crear_debeEliminarArchivoYSiLoteFalla() {
        ProveedorEntity proveedor = proveedorActivo();
        List<CompraDTOs.LoteEntrada> lotes = new ArrayList<>(List.of(
                loteMedicamento(3L, "LOTE-A"),
                loteInsumo(99L, "LOTE-B")));
        CompraDTOs.Request req = requestConLotes(lotes);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(repository.save(any())).thenAnswer(invocation -> {
            CompraEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
        when(comprobanteStorage.guardar(any(), anyLong())).thenReturn("uploads/comprobantes/compra-1-abc.pdf");
        when(loteService.crear(any())).thenReturn(respuestaLoteMock("LOTE-A"))
                .thenThrow(new IllegalArgumentException("Insumo no encontrado con el ID: 99"));

        assertThatThrownBy(() -> service.crear(comprobante, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo no encontrado");

        verify(loteService, times(2)).crear(any());
        verify(comprobanteStorage).eliminar("uploads/comprobantes/compra-1-abc.pdf");
    }

    @Test
    void obtenerPorId_debeRetornarCuandoExiste() {
        CompraEntity entity = CompraEntity.builder()
                .id(1L).codigo("COM-1").total(BigDecimal.TEN).estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedorActivo()).build();
        CompraDTOs.Response resp = new CompraDTOs.Response(1L, "COM-1", BigDecimal.TEN, null, true,
                1L, "Proveedor SA", entity.getFecha(), null);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(resp);

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Compra no encontrada");
    }

    @Test
    void cambiarEstado_debeInvertirEstado() {
        CompraEntity entity = CompraEntity.builder().id(1L).codigo("COM-1").estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedorActivo()).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cambiarEstado(1L);

        assertThat(entity.isEstado()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void actualizar_debeReemplazarComprobante() {
        ProveedorEntity proveedor = proveedorActivo();
        CompraEntity existente = CompraEntity.builder()
                .id(1L).codigo("COM-1").total(BigDecimal.ZERO)
                .comprobante("uploads/comprobantes/compra-1-viejo.pdf").estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedor).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(comprobanteStorage.guardar(any(), eq(1L))).thenReturn("uploads/comprobantes/compra-1-nuevo.pdf");
        when(repository.save(any())).thenReturn(existente);
        when(mapper.toDTO(existente)).thenReturn(new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, "uploads/comprobantes/compra-1-nuevo.pdf", true,
                1L, "Proveedor SA", existente.getFecha(), null));

        CompraDTOs.Response result = service.actualizar(1L, comprobante, null);

        assertThat(result.comprobante()).isEqualTo("uploads/comprobantes/compra-1-nuevo.pdf");
        verify(comprobanteStorage).eliminar("uploads/comprobantes/compra-1-viejo.pdf");
    }

    @Test
    void buscarPorCodigo_retornaVacioSiEstaEnBlanco() {
        assertThat(service.buscarPorCodigo("  ", null)).isEmpty();
        verify(repository, never()).findByCodigoContainingIgnoreCase(any());
    }

    private CompraEntity compraDe(String codigo, BigDecimal total, Long id) {
        return CompraEntity.builder()
                .id(id).codigo(codigo).total(total).estado(true)
                .fecha(LocalDateTime.now()).proveedor(proveedorActivo()).build();
    }

    private unidadMedidaEntity unidadMedidaMock(String nombre, String abreviatura) {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre(nombre);
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

    private LoteEntity loteMedicamentoDeCompra(CompraEntity compra, Long id, String codigoLote, int cantidad) {
        medicamentoLogEntity medicamento = medicamentoLogEntity.builder()
                .id(3L)
                .codigo("MED-0001")
                .nombre("Amoxicilina")
                .dosis(BigDecimal.valueOf(500))
                .estado(true)
                .unidadMedida(unidadMedidaMock("Miligramo", "mg"))
                .marca(marcaMock("Genfar"))
                .presentacion(presentacionMock("Caja x 12"))
                .build();
        return LoteEntity.builder()
                .id(id).codigoLote(codigoLote).cantidad(cantidad).disponible(cantidad).estado(true)
                .fechaVencimiento(LocalDate.now().plusMonths(6))
                .precioCompra(BigDecimal.TEN).precioVenta(BigDecimal.valueOf(15))
                .total(BigDecimal.TEN.multiply(BigDecimal.valueOf(cantidad)))
                .medicamentoLog(medicamento).compra(compra).build();
    }

    private LoteEntity loteInsumoDeCompra(CompraEntity compra, Long id, String codigoLote, int cantidad) {
        insumosLogEntity insumo = insumosLogEntity.builder()
                .id(7L)
                .codigo("INS-0001")
                .nombre("Gasa estéril")
                .detalle("Gasa estéril 10x10 cm")
                .estado(true)
                .marca(marcaMock("3M"))
                .build();
        return LoteEntity.builder()
                .id(id).codigoLote(codigoLote).cantidad(cantidad).disponible(cantidad).estado(true)
                .fechaVencimiento(null)
                .precioCompra(BigDecimal.valueOf(8)).precioVenta(BigDecimal.valueOf(12))
                .total(BigDecimal.valueOf(8).multiply(BigDecimal.valueOf(cantidad)))
                .insumoLog(insumo).compra(compra).build();
    }

    @Test
    void obtenerResumen_debeSumarCantidadDeLotesDeInsumosYMedicamentos() {
        CompraEntity compra1 = compraDe("COM-1", BigDecimal.valueOf(150), 1L);
        CompraEntity compra2 = compraDe("COM-2", BigDecimal.valueOf(40), 2L);
        when(repository.findAll()).thenReturn(List.of(compra1, compra2));
        when(loteRepository.findByCompra_IdIn(List.of(1L, 2L))).thenReturn(List.of(
                loteMedicamentoDeCompra(compra1, 1L, "LOTE-A", 10),
                loteInsumoDeCompra(compra1, 2L, "LOTE-B", 5),
                loteMedicamentoDeCompra(compra2, 3L, "LOTE-C", 7)));

        List<CompraDTOs.ResumenResponse> resumen = service.obtenerResumen(null);

        assertThat(resumen).hasSize(2);
        assertThat(resumen.get(0).id()).isEqualTo(1L);
        assertThat(resumen.get(0).codigo()).isEqualTo("COM-1");
        assertThat(resumen.get(0).proveedor()).isEqualTo("Proveedor SA");
        assertThat(resumen.get(0).fecha()).isEqualTo(compra1.getFecha());
        assertThat(resumen.get(0).cantidadProductos()).isEqualTo(15);
        assertThat(resumen.get(0).total()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(resumen.get(1).cantidadProductos()).isEqualTo(7);
    }

    @Test
    void obtenerResumen_debeRetornarCeroCuandoLaCompraNoTieneLotes() {
        CompraEntity compra = compraDe("COM-1", BigDecimal.TEN, 1L);
        when(repository.findAll()).thenReturn(List.of(compra));
        when(loteRepository.findByCompra_IdIn(List.of(1L))).thenReturn(List.of());

        List<CompraDTOs.ResumenResponse> resumen = service.obtenerResumen(null);

        assertThat(resumen).hasSize(1);
        assertThat(resumen.get(0).cantidadProductos()).isZero();
    }

    @Test
    void obtenerResumen_debeFiltrarPorEstadoCuandoSeEnviaActivos() {
        CompraEntity compra = compraDe("COM-1", BigDecimal.TEN, 1L);
        when(repository.findByEstado(true)).thenReturn(List.of(compra));
        when(loteRepository.findByCompra_IdIn(List.of(1L)))
                .thenReturn(List.of(loteMedicamentoDeCompra(compra, 1L, "LOTE-A", 3)));

        List<CompraDTOs.ResumenResponse> resumen = service.obtenerResumen(true);

        assertThat(resumen).hasSize(1);
        assertThat(resumen.get(0).cantidadProductos()).isEqualTo(3);
        verify(repository, never()).findAll();
    }

    @Test
    void obtenerDetalle_debeRetornarDatosDelMedicamentoYDelInsumo() {
        CompraEntity compra = compraDe("COM-1", BigDecimal.valueOf(150), 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(compra));
        when(loteRepository.findByCompra_Id(1L)).thenReturn(List.of(
                loteMedicamentoDeCompra(compra, 1L, "LOTE-A", 10),
                loteInsumoDeCompra(compra, 2L, "LOTE-B", 5)));

        CompraDTOs.DetalleResponse detalle = service.obtenerDetalle(1L);

        assertThat(detalle.id()).isEqualTo(1L);
        assertThat(detalle.codigo()).isEqualTo("COM-1");
        assertThat(detalle.proveedorId()).isEqualTo(1L);
        assertThat(detalle.proveedorNombre()).isEqualTo("Proveedor SA");
        assertThat(detalle.fecha()).isEqualTo(compra.getFecha());
        assertThat(detalle.total()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(detalle.lotes()).hasSize(2);

        CompraDTOs.LoteDetalleResponse medicamento = detalle.lotes().get(0);
        assertThat(medicamento.id()).isEqualTo(1L);
        assertThat(medicamento.codigoLote()).isEqualTo("LOTE-A");
        assertThat(medicamento.codigo()).isEqualTo("MED-0001");
        assertThat(medicamento.esMedicamento()).isTrue();
        assertThat(medicamento.nombre()).isEqualTo("Amoxicilina");
        assertThat(medicamento.dosis()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(medicamento.unidadMedida()).isEqualTo("mg");
        assertThat(medicamento.fabricante()).isEqualTo("Genfar");
        assertThat(medicamento.presentacion()).isEqualTo("Caja x 12");
        assertThat(medicamento.detalle()).isNull();
        assertThat(medicamento.cantidad()).isEqualTo(10);
        assertThat(medicamento.precioCompra()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(medicamento.fechaVencimiento()).isNotNull();
    }

    @Test
    void obtenerDetalle_debeRetornarDatosDelInsumo() {
        CompraEntity compra = compraDe("COM-1", BigDecimal.valueOf(40), 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(compra));
        when(loteRepository.findByCompra_Id(1L)).thenReturn(List.of(
                loteInsumoDeCompra(compra, 2L, "LOTE-B", 5)));

        CompraDTOs.LoteDetalleResponse insumo = service.obtenerDetalle(1L).lotes().get(0);

        assertThat(insumo.codigoLote()).isEqualTo("LOTE-B");
        assertThat(insumo.codigo()).isEqualTo("INS-0001");
        assertThat(insumo.esMedicamento()).isFalse();
        assertThat(insumo.nombre()).isEqualTo("Gasa estéril");
        assertThat(insumo.fabricante()).isEqualTo("3M");
        assertThat(insumo.detalle()).isEqualTo("Gasa estéril 10x10 cm");
        assertThat(insumo.dosis()).isNull();
        assertThat(insumo.unidadMedida()).isNull();
        assertThat(insumo.presentacion()).isNull();
        assertThat(insumo.cantidad()).isEqualTo(5);
        assertThat(insumo.precioCompra()).isEqualByComparingTo(BigDecimal.valueOf(8));
        assertThat(insumo.fechaVencimiento()).isNull();
    }

    @Test
    void obtenerDetalle_lanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDetalle(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Compra no encontrada");

        verify(loteRepository, never()).findByCompra_Id(anyLong());
    }
}
