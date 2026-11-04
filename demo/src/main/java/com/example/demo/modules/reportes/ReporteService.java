package com.example.demo.modules.reportes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;

    // --- Farmacia ---

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> despachos(LocalDate desde, LocalDate hasta) {
        return reporteRepository.despachosPorProducto(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.CompraProveedor> comprasPorProveedor(LocalDate desde, LocalDate hasta) {
        return reporteRepository.comprasPorProveedor(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.CompraProveedor(
                        texto(fila[0]), entero(fila[1]), decimal(fila[2])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.StockBajo> stockBajo(int umbral) {
        return reporteRepository.stockBajo(umbral).stream()
                .map(fila -> new ReporteDTOs.StockBajo(
                        largo(fila[0]), texto(fila[1]), entero(fila[2]), fecha(fila[3])))
                .toList();
    }

    // --- Pacientes ---

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Censo> censoCamas() {
        return reporteRepository.censoCamas().stream()
                .map(fila -> new ReporteDTOs.Censo(texto(fila[0]), texto(fila[1]), entero(fila[2])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> ingresosPorArea(LocalDate desde, LocalDate hasta) {
        return reporteRepository.ingresosPorArea(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> demografia() {
        return reporteRepository.demografiaPorSexo().stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    // --- Médica ---

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> diagnosticosFrecuentes(LocalDate desde, LocalDate hasta) {
        return reporteRepository.diagnosticosFrecuentes(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> altasPorTipo(LocalDate desde, LocalDate hasta) {
        return reporteRepository.altasPorTipo(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> atencionesPorMedico(LocalDate desde, LocalDate hasta) {
        return reporteRepository.atencionesPorMedico(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    // --- Enfermería ---

    @Transactional(readOnly = true)
    public ReporteDTOs.MonitoreoSummary monitoreo(LocalDate desde, LocalDate hasta) {
        List<Object[]> filas = reporteRepository.monitoreoResumen(inicio(desde), fin(hasta));
        if (filas.isEmpty()) {
            return new ReporteDTOs.MonitoreoSummary(0, 0, 0);
        }
        Object[] fila = filas.get(0);
        return new ReporteDTOs.MonitoreoSummary(entero(fila[0]), entero(fila[1]), entero(fila[2]));
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> tratamientosMedicamentos(LocalDate desde, LocalDate hasta) {
        return reporteRepository.tratamientosMedicamentos(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> tratamientosCuidados(LocalDate desde, LocalDate hasta) {
        return reporteRepository.tratamientosCuidados(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.GlucosaPromedio> glucosaPromedio(LocalDate desde, LocalDate hasta) {
        return reporteRepository.glucosaPromedio(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.GlucosaPromedio(texto(fila[0]), doble(fila[1])))
                .toList();
    }

    // --- Usuarios ---

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> usuariosPorRol() {
        return reporteRepository.usuariosPorRol().stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteDTOs.Conteo> accesos(LocalDate desde, LocalDate hasta) {
        return reporteRepository.accesosPorUsuario(inicio(desde), fin(hasta)).stream()
                .map(fila -> new ReporteDTOs.Conteo(texto(fila[0]), entero(fila[1])))
                .toList();
    }

    // --- Helpers de conversión ---

    private LocalDateTime inicio(LocalDate desde) {
        return (desde != null ? desde : LocalDate.now().minusMonths(1)).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return (hasta != null ? hasta : LocalDate.now()).atTime(LocalTime.MAX);
    }

    private String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private long largo(Object valor) {
        return valor == null ? 0L : ((Number) valor).longValue();
    }

    private int entero(Object valor) {
        return valor == null ? 0 : ((Number) valor).intValue();
    }

    private BigDecimal decimal(Object valor) {
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }

    private double doble(Object valor) {
        return valor == null ? 0.0 : ((Number) valor).doubleValue();
    }

    private LocalDate fecha(Object valor) {
        if (valor instanceof Date fechaSql) {
            return fechaSql.toLocalDate();
        }
        return valor == null ? null : LocalDate.parse(valor.toString());
    }
}
