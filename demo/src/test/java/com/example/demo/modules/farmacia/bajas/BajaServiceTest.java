package com.example.demo.modules.farmacia.bajas;

import com.example.demo.modules.farmacia.insumosLog.insumosLogEntity;
import com.example.demo.modules.farmacia.lotes.LoteEntity;
import com.example.demo.modules.farmacia.lotes.LoteRepository;
import com.example.demo.modules.farmacia.marca.marcaEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaEntity;
import com.example.demo.modules.farmacia.motivoBaja.motivoBajaRepository;
import com.example.demo.modules.farmacia.presentacion.presentacionEntity;
import com.example.demo.modules.farmacia.unidadMedida.unidadMedidaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BajaServiceTest {

    @Mock
    private BajaRepository repository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private motivoBajaRepository motivoBajaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BajaService service;

    private LoteEntity lote(int disponible) {
        return LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(disponible).estado(true)
                .build();
    }

    private motivoBajaEntity motivo() {
        motivoBajaEntity motivo = new motivoBajaEntity();
        motivo.setId(2L);
        motivo.setNombre("Vencimiento");
        motivo.setEstado(true);
        return motivo;
    }

    private UsuarioEntity usuario() {
        PersonaEntity persona = PersonaEntity.builder()
                .primerNombre("Juan")
                .segundoNombre("Carlos")
                .primerApellido("Pérez")
                .segundoApellido("López")
                .build();
        return UsuarioEntity.builder()
                .id(3L).username("jperez").persona(persona).estado(true)
                .build();
    }

    private BajaDTOs.Request request(int cantidad) {
        return new BajaDTOs.Request(1L, cantidad, 2L, "  producto dañado  ", 3L);
    }

    private marcaEntity marca() {
        marcaEntity marca = new marcaEntity();
        marca.setId(1L);
        marca.setNombre("Bayer");
        marca.setEstado(true);
        return marca;
    }

    private presentacionEntity presentacion() {
        presentacionEntity presentacion = new presentacionEntity();
        presentacion.setId(1L);
        presentacion.setNombre("Tabletas");
        presentacion.setEstado(true);
        return presentacion;
    }

    private unidadMedidaEntity unidad() {
        unidadMedidaEntity unidad = new unidadMedidaEntity();
        unidad.setId(1L);
        unidad.setNombre("Miligramo");
        unidad.setAbreviatura("mg");
        unidad.setEstado(true);
        return unidad;
    }

    private LoteEntity loteConMedicamento(int disponible) {
        medicamentoLogEntity medicamento = medicamentoLogEntity.builder()
                .id(9L).nombre("Penicilina").codigo("MED-009")
                .dosis(new BigDecimal("500")).estado(true)
                .marca(marca()).presentacion(presentacion()).unidadMedida(unidad())
                .build();
        return LoteEntity.builder()
                .id(1L).codigoLote("LT-001").cantidad(10).disponible(disponible).estado(true)
                .medicamentoLog(medicamento)
                .build();
    }

    private LoteEntity loteConInsumo(int disponible) {
        insumosLogEntity insumo = insumosLogEntity.builder()
                .id(5L).nombre("Jeringa descartable").codigo("INS-005")
                .detalle("5 ml").estado(true)
                .marca(marca())
                .build();
        return LoteEntity.builder()
                .id(2L).codigoLote("Tk-020").cantidad(20).disponible(disponible).estado(true)
                .insumoLog(insumo)
                .build();
    }

    private BajaEntity baja(LoteEntity lote) {
        return BajaEntity.builder()
                .id(99L).lote(lote).cantidad(4).motivoBaja(motivo())
                .observacion("producto dañado")
                .fechaHora(LocalDateTime.of(2026, 10, 17, 10, 30))
                .usuario(usuario())
                .build();
    }

    @Test
    void registrar_debeDescontarStockYGuardarLaBaja() {
        LoteEntity lote = lote(10);
        motivoBajaEntity motivo = motivo();
        UsuarioEntity usuario = usuario();
        BajaEntity guardada = BajaEntity.builder()
                .id(99L).lote(lote).cantidad(4).motivoBaja(motivo)
                .observacion("producto dañado").fechaHora(java.time.LocalDateTime.now())
                .usuario(usuario).build();

        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(motivoBajaRepository.findById(2L)).thenReturn(Optional.of(motivo));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(loteRepository.save(any())).thenReturn(lote);
        when(repository.save(any())).thenReturn(guardada);

        BajaDTOs.Response result = service.registrar(request(4));

        assertThat(lote.getDisponible()).isEqualTo(6);
        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.idLote()).isEqualTo(1L);
        assertThat(result.codigoLote()).isEqualTo("LT-001");
        assertThat(result.cantidad()).isEqualTo(4);
        assertThat(result.stockAnterior()).isEqualTo(10);
        assertThat(result.stockActual()).isEqualTo(6);
        assertThat(result.idMotivoBaja()).isEqualTo(2L);
        assertThat(result.motivo()).isEqualTo("Vencimiento");
        assertThat(result.observacion()).isEqualTo("producto dañado");
        assertThat(result.idUsuario()).isEqualTo(3L);
        assertThat(result.usuario()).isEqualTo("jperez");
        assertThat(result.fechaHora()).isNotNull();
        verify(loteRepository).save(lote);
        verify(repository).save(argThat(baja -> baja.getCantidad() == 4
                && baja.getLote() == lote
                && baja.getMotivoBaja() == motivo
                && baja.getUsuario() == usuario));
    }

    @Test
    void registrar_debeRechazarCantidadMayorAlStockDelLote() {
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote(3)));

        assertThatThrownBy(() -> service.registrar(request(4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo tiene 3 disponibles");

        verify(repository, never()).save(any());
        verify(loteRepository, never()).save(any());
    }

    @Test
    void registrar_debeRechazarLoteInexistente() {
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(request(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lote no encontrado");

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_debeRechazarMotivoInexistente() {
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote(10)));
        when(motivoBajaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(request(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Motivo de baja no encontrado");

        verify(repository, never()).save(any());
        verify(loteRepository, never()).save(any());
    }

    @Test
    void registrar_debeRechazarUsuarioInexistente() {
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote(10)));
        when(motivoBajaRepository.findById(2L)).thenReturn(Optional.of(motivo()));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(request(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(repository, never()).save(any());
        verify(loteRepository, never()).save(any());
    }

    @Test
    void registrar_debeRechazarCantidadNoPositiva() {
        assertThatThrownBy(() -> service.registrar(request(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a 0");

        assertThatThrownBy(() -> service.registrar(new BajaDTOs.Request(1L, null, 2L, null, 3L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a 0");

        verify(repository, never()).save(any());
    }

    @Test
    void listar_debeRetornarLasBajasConNombreDeProducto() {
        LoteEntity lote = loteConMedicamento(6);
        BajaEntity baja = baja(lote);
        when(repository.findAllByOrderByFechaHoraDesc()).thenReturn(List.of(baja));

        List<BajaDTOs.ListadoResponse> result = service.listar(null);

        assertThat(result).hasSize(1);
        BajaDTOs.ListadoResponse fila = result.get(0);
        assertThat(fila.idBaja()).isEqualTo(99L);
        assertThat(fila.producto()).isEqualTo("Penicilina Bayer Tabletas 500mg");
        assertThat(fila.lote()).isEqualTo("LT-001");
        assertThat(fila.motivo()).isEqualTo("Vencimiento");
        assertThat(fila.observacion()).isEqualTo("producto dañado");
        assertThat(fila.fecha()).isEqualTo(LocalDateTime.of(2026, 10, 17, 10, 30));
        assertThat(fila.usuario()).isEqualTo("Juan Pérez López");
    }

    @Test
    void listar_debeFiltrarPorTipoMedicamentoEInsumo() {
        BajaEntity medicamento = baja(loteConMedicamento(6));
        LoteEntity loteInsumo = loteConInsumo(12);
        BajaEntity insumo = BajaEntity.builder()
                .id(100L).lote(loteInsumo).cantidad(2).motivoBaja(motivo())
                .observacion(null)
                .fechaHora(LocalDateTime.of(2026, 10, 17, 11, 0))
                .usuario(usuario())
                .build();
        when(repository.findAllByOrderByFechaHoraDesc())
                .thenReturn(List.of(medicamento, insumo));

        assertThat(service.listar("medicamento"))
                .extracting(BajaDTOs.ListadoResponse::idBaja)
                .containsExactly(99L);
        assertThat(service.listar("insumo"))
                .extracting(BajaDTOs.ListadoResponse::idBaja)
                .containsExactly(100L);
        assertThat(service.listar(null)).hasSize(2);

        BajaDTOs.ListadoResponse filaInsumo = service.listar("insumo").get(0);
        assertThat(filaInsumo.producto()).isEqualTo("Jeringa descartable Bayer 5 ml");
        assertThat(filaInsumo.lote()).isEqualTo("Tk-020");

        assertThatThrownBy(() -> service.listar("otro"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");
    }

    @Test
    void revertir_debeRestaurarElStockYEliminarLaBaja() {
        LoteEntity lote = lote(6);
        BajaEntity baja = baja(lote);
        when(repository.findById(99L)).thenReturn(Optional.of(baja));
        when(loteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenReturn(lote);

        service.revertir(99L);

        assertThat(lote.getDisponible()).isEqualTo(10);
        verify(loteRepository).save(lote);
        verify(repository).delete(baja);
    }

    @Test
    void revertir_debeRechazarBajaInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revertir(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Baja no encontrada");

        verify(loteRepository, never()).save(any());
        verify(repository, never()).delete(any());
    }
}
