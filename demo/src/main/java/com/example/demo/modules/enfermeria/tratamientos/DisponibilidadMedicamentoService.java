package com.example.demo.modules.enfermeria.tratamientos;

import com.example.demo.modules.clinica.indicaciones.medicamentos.EstadoIndicacionMedicamento;
import com.example.demo.modules.clinica.indicaciones.medicamentos.indicacionMedicamentoRepository;
import com.example.demo.modules.clinica.procedimientoMedico.TipoRecursoProcedimiento;
import com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento.EstadoAplicacionMedicamento;
import com.example.demo.modules.enfermeria.tratamientos.aplicacionesMedicamento.aplicacionMedicamentoRepository;
import com.example.demo.modules.farmacia.salidas.SolicitudSalidaMovimientoRepository;
import com.example.demo.modules.farmacia.salidas.TipoMovimientoSalida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Disponibilidad "en piso" de los medicamentos del catálogo para un episodio: lo que Farmacia
 * entregó menos lo ya administrado y lo devuelto. Es la fuente de verdad para pintar la tarjeta,
 * bloquear la administración sin stock y limitar las devoluciones.
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadMedicamentoService {

    private final aplicacionMedicamentoRepository aplicacionRepository;
    private final SolicitudSalidaMovimientoRepository movimientoRepository;
    private final indicacionMedicamentoRepository indicacionRepository;

    @Transactional(readOnly = true)
    public List<disponibilidadMedicamentoDTOs.Response> disponibilidadPorEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        return indicacionRepository
                .findDistinctMedicamentoLogIds(episodioId, EstadoIndicacionMedicamento.ACTIVO)
                .stream()
                .map(id -> new disponibilidadMedicamentoDTOs.Response(
                        id, disponibleEnPiso(episodioId, id)))
                .toList();
    }

    /** Cantidad en piso de un medicamento: entregado − devuelto − aplicado (nunca negativa). */
    @Transactional(readOnly = true)
    public int disponibleEnPiso(Long episodioId, Long medicamentoLogId) {
        long entregado = valor(movimientoRepository.sumarCantidadPorRecurso(
                episodioId, TipoRecursoProcedimiento.MEDICAMENTO, medicamentoLogId,
                TipoMovimientoSalida.ENTREGA));
        long devuelto = valor(movimientoRepository.sumarCantidadPorRecurso(
                episodioId, TipoRecursoProcedimiento.MEDICAMENTO, medicamentoLogId,
                TipoMovimientoSalida.DEVOLUCION));
        long aplicado = valor(aplicacionRepository.sumarCantidadAdministrada(
                episodioId, medicamentoLogId, EstadoAplicacionMedicamento.ADMINISTRADO));

        return (int) Math.max(0, entregado - devuelto - aplicado);
    }

    /** Cantidad ya administrada de un medicamento dentro del episodio. */
    @Transactional(readOnly = true)
    public int aplicado(Long episodioId, Long medicamentoLogId) {
        return (int) valor(aplicacionRepository.sumarCantidadAdministrada(
                episodioId, medicamentoLogId, EstadoAplicacionMedicamento.ADMINISTRADO));
    }

    private long valor(Long numero) {
        return numero == null ? 0 : numero;
    }
}
