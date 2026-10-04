package com.example.demo.modules.farmacia.medicamentoLog;

import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.presentacion.presentacionRepository;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaRepository;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminEntity;
import com.example.demo.modules.farmacia.viaAdmin.viaAdminRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        List<medicamentoLogDTOs.MedicamentoLogResponse> resultado = service.obtenerTodos(true);

        assertEquals("mg", resultado.get(0).unidadMedida());
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
        when(repository.findById(7L)).thenReturn(Optional.of(medicamento));

        service.cambiarEstado(7L);

        org.mockito.ArgumentCaptor<medicamentoLogEntity> captor =
                org.mockito.ArgumentCaptor.forClass(medicamentoLogEntity.class);
        verify(repository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertFalse(captor.getValue().isEstado());
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