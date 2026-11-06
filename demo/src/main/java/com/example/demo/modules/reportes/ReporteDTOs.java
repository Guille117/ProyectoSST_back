package com.example.demo.modules.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ReporteDTOs {

    private ReporteDTOs() {
    }

    public record Conteo(String nombre, long cantidad) {
    }

    public record CompraProveedor(String proveedor, long cantidad, BigDecimal total) {
    }

    public record StockBajo(long id, String producto, int disponible, LocalDate vencimiento) {
    }

    public record Vencimiento(String producto, long cantidad, int mes, int anio) {
    }

    public record Censo(String area, String estado, long cantidad) {
    }

    public record MonitoreoSummary(long signosVitales, long glucosa, long controlHidrico) {
    }

    public record GlucosaPromedio(String tipo, double promedio) {
    }
}
