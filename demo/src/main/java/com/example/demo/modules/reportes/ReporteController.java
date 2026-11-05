package com.example.demo.modules.reportes;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // --- Farmacia ---

    @GetMapping("/farmacia/despachos")
    public List<ReporteDTOs.Conteo> despachos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.despachos(desde, hasta);
    }

    @GetMapping("/farmacia/mas-vendidos")
    public List<ReporteDTOs.Conteo> masVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.masVendidos(desde, hasta);
    }

    @GetMapping("/farmacia/menos-vendidos")
    public List<ReporteDTOs.Conteo> menosVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.menosVendidos(desde, hasta);
    }

    @GetMapping("/farmacia/vencimientos")
    public List<ReporteDTOs.Vencimiento> vencimientos() {
        return reporteService.vencimientos();
    }

    @GetMapping("/farmacia/compras-por-proveedor")
    public List<ReporteDTOs.CompraProveedor> comprasPorProveedor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.comprasPorProveedor(desde, hasta);
    }

    @GetMapping("/farmacia/stock-bajo")
    public List<ReporteDTOs.StockBajo> stockBajo(@RequestParam(defaultValue = "5") int umbral) {
        return reporteService.stockBajo(umbral);
    }

    // --- Pacientes ---

    @GetMapping("/pacientes/censo")
    public List<ReporteDTOs.Censo> censoCamas() {
        return reporteService.censoCamas();
    }

    @GetMapping("/pacientes/ingresos-por-area")
    public List<ReporteDTOs.Conteo> ingresosPorArea(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.ingresosPorArea(desde, hasta);
    }

    @GetMapping("/pacientes/demografia")
    public List<ReporteDTOs.Conteo> demografia() {
        return reporteService.demografia();
    }

    // --- Médica ---

    @GetMapping("/medica/diagnosticos-frecuentes")
    public List<ReporteDTOs.Conteo> diagnosticosFrecuentes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.diagnosticosFrecuentes(desde, hasta);
    }

    @GetMapping("/medica/altas-por-tipo")
    public List<ReporteDTOs.Conteo> altasPorTipo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.altasPorTipo(desde, hasta);
    }

    @GetMapping("/medica/atenciones-por-medico")
    public List<ReporteDTOs.Conteo> atencionesPorMedico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.atencionesPorMedico(desde, hasta);
    }

    // --- Enfermería ---

    @GetMapping("/enfermeria/monitoreo")
    public ReporteDTOs.MonitoreoSummary monitoreo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.monitoreo(desde, hasta);
    }

    @GetMapping("/enfermeria/tratamientos-medicamentos")
    public List<ReporteDTOs.Conteo> tratamientosMedicamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.tratamientosMedicamentos(desde, hasta);
    }

    @GetMapping("/enfermeria/tratamientos-cuidados")
    public List<ReporteDTOs.Conteo> tratamientosCuidados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.tratamientosCuidados(desde, hasta);
    }

    @GetMapping("/enfermeria/glucosa-promedio")
    public List<ReporteDTOs.GlucosaPromedio> glucosaPromedio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.glucosaPromedio(desde, hasta);
    }

    // --- Usuarios ---

    @GetMapping("/usuarios/por-rol")
    public List<ReporteDTOs.Conteo> usuariosPorRol() {
        return reporteService.usuariosPorRol();
    }

    @GetMapping("/usuarios/accesos")
    public List<ReporteDTOs.Conteo> accesos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.accesos(desde, hasta);
    }
}
