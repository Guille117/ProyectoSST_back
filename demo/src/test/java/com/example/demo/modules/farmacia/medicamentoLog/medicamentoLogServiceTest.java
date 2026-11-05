package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.config.ConflictException;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoRepository;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.presentacion.presentacionRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaRepository;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminEntity;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.security.UsuarioActualService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class medicamentoLogServiceTest {

    @Mock
    private medicamentoLogRepository repository;
    @Mock
    private unidadMedidaRepository unidadMedidaRepository;
    @Mock
    private marcaRepository marcaRepository;
    @Mock
    private viaAdminRepository viaAdminRepository;
    @Mock
    private presentacionRepository presentacionRepository;
    @Mock
    private motivoBajaRepository motivoBajaRepository;
    @Mock
    private LoteRepository loteRepository;
    @Mock
    private indicacionMedicamentoRepository indicacionMedicamentoRepository;
    @Mock
    private procedimientoMedicoRepository procedimientoMedicoRepository;
    @Mock
    private medicamentoCorreccionRepository correccionRepository;
    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private medicamentoLogService service;

    @BeforeEach
    void prepararCatalogos() {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre("mg");
        lenient().when(unidadMedidaRepository.findById(1L)).thenReturn(Optional.of(unidad));

        marcaEntity marca = new marcaEntity();
        marca.setId(1L);
        marca.setNombre("Marca A");
        lenient().when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));

        viaAdminEntity via = new viaAdminEntity();
        via.setId(1L);
        via.setNombre("Oral");
        lenient().when(viaAdminRepository.findById(1L)).thenReturn(Optional.of(via));

        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(1L);
        presentacion.setNombre("Tableta");
        lenient().when(presentacionRepository.findById(1L)).thenReturn(Optional.of(presentacion));

    }

    @Test
    void obtenerConteoCatalogosFarmacia_debeContarLosCincoCatalogos() {
        when(unidadMedidaRepository.count()).thenReturn(3L);
        when(marcaRepository.count()).thenReturn(4L);
        when(viaAdminRepository.count()).thenReturn(5L);
        when(presentacionRepository.count()).thenReturn(6L);
        when(motivoBajaRepository.count()).thenReturn(7L);

        var conteo = service.obtenerConteoCatalogosFarmacia();

        assertEquals(3L, conteo.get("unidad_medidas"));
        assertEquals(4L, conteo.get("marcas"));
        assertEquals(5L, conteo.get("vias_admin"));
        assertEquals(6L, conteo.get("presentaciones"));
        assertEquals(7L, conteo.get("motivoBaja"));
    }

    @Test
    void crear_debePermitirMismoNombreConDosisDiferente() {
        medicamentoLogDTOs.Request request = request("200");
        when(repository.findByIdentidad("Amoxicilina", new BigDecimal("200"), 1L, 1L, 1L, 1L))
                .thenReturn(Optional.empty());
        when(repository.save(any(medicamentoLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        medicamentoLogDTOs.Response response = service.crear(request);

        assertEquals("Amoxicilina", response.nombre());
        assertEquals(new BigDecimal("200"), response.dosis());
        verify(repository).save(any(medicamentoLogEntity.class));
    }

        @Test
        void crear_debePermitirMismoMedicamentoConMarcaDiferente() {
            marcaEntity otraMarca = new marcaEntity();
            otraMarca.setId(2L);
            otraMarca.setNombre("Marca B");
            when(marcaRepository.findById(2L)).thenReturn(Optional.of(otraMarca));
        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
            "Amoxicilina", new BigDecimal("500"), 1L, 2L, 1L, 1L);
        when(repository.findByIdentidad("Amoxicilina", new BigDecimal("500"), 1L, 2L, 1L, 1L))
            .thenReturn(Optional.empty());
        when(repository.save(any(medicamentoLogEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        medicamentoLogDTOs.Response response = service.crear(request);

        assertEquals("Marca B", response.marca());
        verify(repository).save(any(medicamentoLogEntity.class));
        }

        @Test
        void crear_debePermitirMismoMedicamentoConPresentacionDiferente() {
            presentacionEntity otraPresentacion = new presentacionEntity();
            otraPresentacion.setId(2L);
            otraPresentacion.setNombre("Cápsula");
            when(presentacionRepository.findById(2L)).thenReturn(Optional.of(otraPresentacion));
        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
            "Amoxicilina", new BigDecimal("500"), 1L, 1L, 1L, 2L);
        when(repository.findByIdentidad("Amoxicilina", new BigDecimal("500"), 1L, 1L, 1L, 2L))
            .thenReturn(Optional.empty());
        when(repository.save(any(medicamentoLogEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        medicamentoLogDTOs.Response response = service.crear(request);

        assertEquals("Cápsula", response.presentacion());
        verify(repository).save(any(medicamentoLogEntity.class));
        }

    @Test
    void crear_debeRechazarMismaIdentidad() {
        medicamentoLogDTOs.Request request = request("500");
        medicamentoLogEntity existente = new medicamentoLogEntity();
        existente.setId(10L);
        when(repository.findByIdentidad("Amoxicilina", new BigDecimal("500"), 1L, 1L, 1L, 1L))
                .thenReturn(Optional.of(existente));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(request)
        );

        assertEquals("Ya existe un medicamento con los mismos datos", exception.getMessage());
        verify(repository, never()).save(any(medicamentoLogEntity.class));
    }

    @Test
    void obtenerTodos_debeRetornarAbreviaturaDeUnidadMedida() {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre("Miligramos");
        unidad.setAbreviatura("mg");

        marcaEntity marca = new marcaEntity();
        marca.setId(1L);
        marca.setNombre("Marca A");

        viaAdminEntity via = new viaAdminEntity();
        via.setId(1L);
        via.setNombre("Oral");

        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(1L);
        presentacion.setNombre("Tableta");

        medicamentoLogEntity medicamento = medicamentoLogEntity.builder()
                .nombre("Amoxicilina")
                .dosis(new BigDecimal("500"))
                .unidadMedida(unidad)
                .marca(marca)
                .viaAdmin(via)
                .presentacion(presentacion)
                .build();
        when(repository.findByEstado(true)).thenReturn(List.of(medicamento));
        when(loteRepository.sumDisponibleMedicamento(any(), any())).thenReturn(15L);

        List<medicamentoLogDTOs.MedicamentoLogResponse> resultado = service.obtenerTodos(true);

        assertEquals("mg", resultado.get(0).unidadMedida());
        assertEquals(15, resultado.get(0).disponible());
        assertEquals(true, resultado.get(0).estado());
    }

    @Test
    void buscarPorId_debeRetornarIdsParaRequest() {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(3L);
        unidad.setAbreviatura("mg");
        marcaEntity marca = new marcaEntity();
        marca.setId(6L);
        marca.setNombre("Marca A");
        viaAdminEntity via = new viaAdminEntity();
        via.setId(4L);
        via.setNombre("Oral");
        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(5L);
        presentacion.setNombre("Tableta");

        medicamentoLogEntity medicamento = medicamentoLogEntity.builder()
            .id(7L)
                .nombre("Amoxicilina")
                .dosis(new BigDecimal("500"))
                .unidadMedida(unidad)
                .marca(marca)
                .viaAdmin(via)
                .presentacion(presentacion)
                .build();
        when(repository.findByIdAndEstado(7L, true)).thenReturn(Optional.of(medicamento));

        medicamentoLogDTOs.MedicamentoLogBusquedaResponse resultado = service.buscarPorId(7L, true);

        assertEquals("Amoxicilina", resultado.nombre());
        assertEquals(new BigDecimal("500"), resultado.dosis());
        assertEquals(3L, resultado.unidadMedidaId());
        assertEquals(4L, resultado.viaAdminId());
        assertEquals(5L, resultado.presentacionId());
        assertEquals(6L, resultado.marcaId());
    }

    @Test
    void buscar_debeAplicarNombreYFiltrosDeCatalogo() {
        unidadMedidaEntity unidad = unidadConId(1L);
        unidad.setAbreviatura("mg");
        marcaEntity marca = marcaConId(2L);
        marca.setNombre("Marca A");
        viaAdminEntity via = viaConId(4L);
        via.setNombre("Oral");
        presentacionEntity presentacion = presentacionConId(3L);
        presentacion.setNombre("Tableta");
        medicamentoLogEntity medicamento = medicamentoLogEntity.builder()
            .id(7L)
                .nombre("Amoxicilina 500")
                .dosis(new BigDecimal("500"))
            .unidadMedida(unidad)
            .marca(marca)
            .viaAdmin(via)
            .presentacion(presentacion)
                .build();
        when(repository.buscar("amox", 2L, 3L, 4L, true)).thenReturn(List.of(medicamento));

        List<medicamentoLogDTOs.MedicamentoLogResponse> resultado =
                service.buscar(" amox ", 2L, 3L, 4L, true);

        assertEquals(1, resultado.size());
        assertEquals("Amoxicilina 500", resultado.get(0).nombre());
        assertEquals(7L, resultado.get(0).id());
        assertEquals(new BigDecimal("500"), resultado.get(0).dosis());
        assertEquals("Marca A", resultado.get(0).marca());
        assertEquals("Tableta", resultado.get(0).presentacion());
        assertEquals("Oral", resultado.get(0).viaAdministracion());
        assertEquals("mg", resultado.get(0).unidadMedida());
        assertEquals(true, resultado.get(0).estado());
    }

    @Test
    void buscar_sinFiltros_debeRetornarListaVacia() {
        assertEquals(List.of(), service.buscar(null, null, null, null, true));
        verify(repository, never()).buscar(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void cambiarEstado_debeAlternarEstadoDelMedicamento() {
        medicamentoLogEntity medicamento = new medicamentoLogEntity();
        medicamento.setId(7L);
        medicamento.setEstado(true);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(medicamento));
        when(loteRepository.sumExistenciasMedicamento(7L)).thenReturn(0L);

        service.cambiarEstado(7L);

        org.mockito.ArgumentCaptor<medicamentoLogEntity> captor =
                org.mockito.ArgumentCaptor.forClass(medicamentoLogEntity.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertFalse(captor.getValue().isEstado());
    }

    @Test
    void cambiarEstado_debeRechazarDeshabilitarConExistencias() {
        medicamentoLogEntity medicamento = medicamentoConIdentidad(7L, "Amoxicilina", true);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(medicamento));
        when(loteRepository.sumExistenciasMedicamento(7L)).thenReturn(12L);

        ConflictException exception = assertThrows(
                ConflictException.class, () -> service.cambiarEstado(7L));

        assertEquals("No se puede deshabilitar el medicamento: existen 12 unidades pendientes en farmacia.",
                exception.getMessage());
        verify(repository, never()).save(any(medicamentoLogEntity.class));
    }

    @Test
    void cambiarEstado_debePermitirHabilitarAunqueHayaExistencias() {
        medicamentoLogEntity medicamento = medicamentoConIdentidad(7L, "Amoxicilina", false);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(medicamento));

        service.cambiarEstado(7L);

        org.junit.jupiter.api.Assertions.assertTrue(medicamento.isEstado());
        verify(repository).save(medicamento);
    }

    @Test
    void actualizar_debeBloquearCambioDeIdentidadCuandoHayHistorial() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Amoxicilina", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(loteRepository.existsByMedicamentoLog_Id(7L)).thenReturn(true);

        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
                "Amoxicilina", new BigDecimal("1000"), 1L, 1L, 1L, 1L);

        ConflictException exception = assertThrows(
                ConflictException.class, () -> service.actualizar(7L, request));

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("historial"));
        verify(repository, never()).save(any(medicamentoLogEntity.class));
    }

    @Test
    void actualizar_debePermitirCuandoHayHistorialPeroNoCambiaLaIdentidad() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Amoxicilina", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(loteRepository.existsByMedicamentoLog_Id(7L)).thenReturn(true);
        when(repository.findByIdentidad(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(medicamentoLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
                "Amoxicilina", new BigDecimal("500"), 1L, 1L, 1L, 1L);

        medicamentoLogDTOs.Response response = service.actualizar(7L, request);

        assertEquals("Amoxicilina", response.nombre());
        verify(repository).save(existente);
    }

    @Test
    void actualizar_debeBloquearCuandoHayHistorialEnIndicaciones() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Amoxicilina", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(loteRepository.existsByMedicamentoLog_Id(7L)).thenReturn(false);
        when(indicacionMedicamentoRepository.existsByMedicamentoLog_Id(7L)).thenReturn(true);

        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
                "Amoxicilina", new BigDecimal("500"), 1L, 2L, 1L, 1L);

        assertThrows(ConflictException.class, () -> service.actualizar(7L, request));
        verify(procedimientoMedicoRepository, never())
                .existsByRecursos_TipoAndRecursos_ReferenciaId(any(), any());
    }

    @Test
    void actualizar_debeBloquearCuandoHayHistorialEnProcedimientos() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Amoxicilina", true);
        marcaEntity otraMarca = new marcaEntity();
        otraMarca.setId(2L);
        otraMarca.setNombre("Marca B");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(loteRepository.existsByMedicamentoLog_Id(7L)).thenReturn(false);
        when(indicacionMedicamentoRepository.existsByMedicamentoLog_Id(7L)).thenReturn(false);
        when(procedimientoMedicoRepository.existsByRecursos_TipoAndRecursos_ReferenciaId(
                TipoRecursoProcedimiento.MEDICAMENTO, 7L)).thenReturn(true);

        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
                "Amoxicilina", new BigDecimal("500"), 1L, 2L, 1L, 1L);

        assertThrows(ConflictException.class, () -> service.actualizar(7L, request));
    }

    @Test
    void corregir_debeAuditarYActualizarElNombre() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Azetaminofen", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByIdentidad(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(3L);
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(repository.save(any(medicamentoLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        medicamentoLogDTOs.CorreccionRequest request = new medicamentoLogDTOs.CorreccionRequest(
                "Acetaminofén", "Error de tipeo");

        medicamentoLogDTOs.Response response = service.corregir(7L, request);

        assertEquals("Acetaminofén", response.nombre());
        assertEquals("Acetaminofén", existente.getNombre());
        ArgumentCaptor<medicamentoCorreccionEntity> captor =
                ArgumentCaptor.forClass(medicamentoCorreccionEntity.class);
        verify(correccionRepository).save(captor.capture());
        assertEquals("Azetaminofen", captor.getValue().getNombreAnterior());
        assertEquals("Acetaminofén", captor.getValue().getNombreNuevo());
        assertEquals("Error de tipeo", captor.getValue().getMotivo());
        assertEquals(3L, captor.getValue().getUsuario().getId());
    }

    @Test
    void corregir_debeExigirMotivo() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Azetaminofen", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));

        medicamentoLogDTOs.CorreccionRequest request = new medicamentoLogDTOs.CorreccionRequest(
                "Acetaminofén", "   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.corregir(7L, request));

        assertEquals("El motivo de la corrección es obligatorio", exception.getMessage());
        verify(correccionRepository, never()).save(any());
    }

    @Test
    void corregir_debeRechazarNombreIgual() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Acetaminofén", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));

        medicamentoLogDTOs.CorreccionRequest request = new medicamentoLogDTOs.CorreccionRequest(
                "acetaminofén", "Sin cambios");

        assertThrows(IllegalArgumentException.class, () -> service.corregir(7L, request));
        verify(correccionRepository, never()).save(any());
    }

    @Test
    void corregir_debeRechazarUsuarioNoIdentificado() {
        medicamentoLogEntity existente = medicamentoConIdentidad(7L, "Azetaminofen", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByIdentidad(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(usuarioActualService.actual()).thenReturn(Optional.empty());

        medicamentoLogDTOs.CorreccionRequest request = new medicamentoLogDTOs.CorreccionRequest(
                "Acetaminofén", "Error de tipeo");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.corregir(7L, request));

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("usuario"));
        verify(correccionRepository, never()).save(any());
    }

    private medicamentoLogEntity medicamentoConIdentidad(Long id, String nombre, boolean estado) {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre("Miligramos");
        unidad.setAbreviatura("mg");
        marcaEntity marca = new marcaEntity();
        marca.setId(1L);
        marca.setNombre("Marca A");
        viaAdminEntity via = new viaAdminEntity();
        via.setId(1L);
        via.setNombre("Oral");
        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(1L);
        presentacion.setNombre("Tableta");
        return medicamentoLogEntity.builder()
                .id(id)
                .nombre(nombre)
                .codigo("MED-1")
                .dosis(new BigDecimal("500"))
                .unidadMedida(unidad)
                .marca(marca)
                .viaAdmin(via)
                .presentacion(presentacion)
                .estado(estado)
                .build();
    }

    private medicamentoLogDTOs.Request request(String dosis) {
        return new medicamentoLogDTOs.Request(
                "Amoxicilina", new BigDecimal(dosis), 1L, 1L, 1L, 1L);
    }

    private unidadMedidaEntity unidadConId(Long id) {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(id);
        return unidad;
    }

    private marcaEntity marcaConId(Long id) {
        marcaEntity marca = new marcaEntity();
        marca.setId(id);
        return marca;
    }

    private viaAdminEntity viaConId(Long id) {
        viaAdminEntity via = new viaAdminEntity();
        via.setId(id);
        return via;
    }

    private presentacionEntity presentacionConId(Long id) {
        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(id);
        return presentacion;
    }
}