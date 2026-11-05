package com.example.demo.modules.farmacia.insumosLog;

import com.example.demo.config.ConflictException;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRepository;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.marca.marcaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.security.UsuarioActualService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class insumosLogServiceTest {

    @Mock
    private insumosLogRepository repository;

    @Mock
    private marcaRepository marcaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private procedimientoMedicoRepository procedimientoMedicoRepository;

    @Mock
    private insumoCorreccionRepository correccionRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private insumosLogService service;

    @Test
    void obtenerTodos_debeIncluirMarcaAnidadaYEstado() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByEstado(true)).thenReturn(List.of(insumo));

        List<insumosLogDTOs.ListadoResponse> resultado = service.obtenerTodos(true);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Guantes", resultado.get(0).nombre());
        assertEquals("Uso médico", resultado.get(0).detalle());
        assertEquals("Marca A", resultado.get(0).marca().nombreMarca());
        assertEquals(4L, resultado.get(0).marca().idMarca());
        assertEquals(true, resultado.get(0).estado());
    }

    @Test
    void buscar_debeFiltrarPorNombreYMarca() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByNombreContainingIgnoreCaseAndMarca_IdAndEstado("guan", 4L, true))
                .thenReturn(List.of(insumo));

        List<insumosLogDTOs.ListadoResponse> resultado = service.buscar(" guan ", 4L, true);

        assertEquals(1, resultado.size());
        assertEquals("Uso médico", resultado.get(0).detalle());
        assertEquals("Marca A", resultado.get(0).marca().nombreMarca());
        assertEquals(4L, resultado.get(0).marca().idMarca());
    }

    @Test
    void cambiarEstado_debeAlternarEstado() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(insumo));
        when(loteRepository.sumExistenciasInsumo(10L)).thenReturn(0L);

        service.cambiarEstado(10L);

        verify(repository).save(insumo);
        assertFalse(insumo.isEstado());
    }

    @Test
    void cambiarEstado_debeRechazarDeshabilitarConExistencias() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(insumo));
        when(loteRepository.sumExistenciasInsumo(10L)).thenReturn(25L);

        ConflictException exception = assertThrows(
                ConflictException.class, () -> service.cambiarEstado(10L));

        assertEquals("No se puede deshabilitar el insumo: existen 25 unidades pendientes en farmacia.",
                exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void cambiarEstado_debePermitirHabilitarAunqueHayaExistencias() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", false);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(insumo));

        service.cambiarEstado(10L);

        assertTrue(insumo.isEstado());
        verify(repository).save(insumo);
    }

    @Test
    void actualizar_debeBloquearCambioDeIdentidadCuandoHayHistorial() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));
        when(loteRepository.existsByInsumoLog_Id(10L)).thenReturn(true);

        insumosLogDTOs.Request request = new insumosLogDTOs.Request("Guantes", 5L, "Uso médico");

        ConflictException exception = assertThrows(
                ConflictException.class, () -> service.actualizar(10L, request));

        assertTrue(exception.getMessage().contains("historial"));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debePermitirCuandoHayHistorialPeroNoCambiaLaIdentidad() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));
        when(loteRepository.existsByInsumoLog_Id(10L)).thenReturn(true);
        when(repository.findByNombreIgnoreCase("Guantes")).thenReturn(Optional.of(insumo));
        when(marcaRepository.findById(4L)).thenReturn(Optional.of(insumo.getMarca()));
        when(repository.save(any(insumosLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        insumosLogDTOs.Request request = new insumosLogDTOs.Request("Guantes", 4L, "Uso médico");

        insumosLogDTOs.Response response = service.actualizar(10L, request);

        assertEquals("Guantes", response.nombre());
        verify(repository).save(insumo);
    }

    @Test
    void corregir_debeAuditarYActualizarElNombre() {
        insumosLogEntity insumo = insumo(10L, "Gaza", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));
        when(repository.findByNombreIgnoreCase("Gasa")).thenReturn(Optional.empty());
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(3L);
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(repository.save(any(insumosLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        insumosLogDTOs.CorreccionRequest request = new insumosLogDTOs.CorreccionRequest(
                "Gasa", "Error de tipeo");

        insumosLogDTOs.Response response = service.corregir(10L, request);

        assertEquals("Gasa", response.nombre());
        ArgumentCaptor<insumoCorreccionEntity> captor =
                ArgumentCaptor.forClass(insumoCorreccionEntity.class);
        verify(correccionRepository).save(captor.capture());
        assertEquals("Gaza", captor.getValue().getNombreAnterior());
        assertEquals("Gasa", captor.getValue().getNombreNuevo());
        assertEquals("Error de tipeo", captor.getValue().getMotivo());
        assertEquals(3L, captor.getValue().getUsuario().getId());
    }

    @Test
    void corregir_debeExigirMotivo() {
        insumosLogEntity insumo = insumo(10L, "Gaza", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));

        insumosLogDTOs.CorreccionRequest request = new insumosLogDTOs.CorreccionRequest(
                "Gasa", "  ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.corregir(10L, request));

        assertEquals("El motivo de la corrección es obligatorio", exception.getMessage());
        verify(correccionRepository, never()).save(any());
    }

    @Test
    void corregir_debeRechazarNombreIgual() {
        insumosLogEntity insumo = insumo(10L, "Gasa", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));

        insumosLogDTOs.CorreccionRequest request = new insumosLogDTOs.CorreccionRequest(
                "gasa", "Sin cambios");

        assertThrows(IllegalArgumentException.class, () -> service.corregir(10L, request));
        verify(correccionRepository, never()).save(any());
    }

    @Test
    void actualizar_debeBloquearCuandoHayHistorialEnProcedimientos() {
        insumosLogEntity insumo = insumo(10L, "Guantes", 4L, "Marca A", true);
        when(repository.findById(10L)).thenReturn(Optional.of(insumo));
        when(loteRepository.existsByInsumoLog_Id(10L)).thenReturn(false);
        when(procedimientoMedicoRepository.existsByRecursos_TipoAndRecursos_ReferenciaId(
                TipoRecursoProcedimiento.INSUMO, 10L)).thenReturn(true);

        insumosLogDTOs.Request request = new insumosLogDTOs.Request("Guantes", 5L, "Uso médico");

        assertThrows(ConflictException.class, () -> service.actualizar(10L, request));
    }

    @Test
    void buscar_sinFiltros_debeRetornarListaVacia() {
        assertEquals(List.of(), service.buscar(null, null, true));
        verify(repository, never()).findByEstado(any(Boolean.class));
    }

    private insumosLogEntity insumo(Long id, String nombre, Long marcaId, String marcaNombre, boolean estado) {
        marcaEntity marca = new marcaEntity();
        marca.setId(marcaId);
        marca.setNombre(marcaNombre);
        return insumosLogEntity.builder()
                .id(id)
                .nombre(nombre)
                .detalle("Uso médico")
                .marca(marca)
                .estado(estado)
                .build();
    }
}