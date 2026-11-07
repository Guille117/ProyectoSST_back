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
    public List<ReporteDTOs.ProductoConteo> masVendidos(
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

    // --- Reportes adicionales ---

    @GetMapping("/farmacia/consumo-area")
    public List<ReporteDTOs.Conteo> consumoPorArea(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.consumoPorArea(desde, hasta);
    }

    @GetMapping("/farmacia/rotacion")
    public List<ReporteDTOs.Conteo> rotacionInventario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.rotacionInventario(desde, hasta);
    }

    @GetMapping("/pacientes/egresos-por-area")
    public List<ReporteDTOs.Conteo> egresosPorArea(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.egresosPorArea(desde, hasta);
    }

    @GetMapping("/pacientes/grupos-edad")
    public List<ReporteDTOs.Conteo> pacientesPorGrupoEdad() {
        return reporteService.pacientesPorGrupoEdad();
    }

    @GetMapping("/pacientes/estancia-promedio")
    public List<ReporteDTOs.Conteo> estanciaPromedio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.estanciaPromedio(desde, hasta);
    }

    @GetMapping("/pacientes/ocupacion-camas")
    public List<ReporteDTOs.Conteo> ocupacionCamas() {
        return reporteService.ocupacionCamas();
    }

    @GetMapping("/medica/procedimientos-realizados")
    public List<ReporteDTOs.Conteo> procedimientosRealizados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.procedimientosRealizados(desde, hasta);
    }

    @GetMapping("/medica/indicaciones-por-tipo")
    public List<ReporteDTOs.Conteo> indicacionesPorTipo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.indicacionesPorTipo(desde, hasta);
    }

    @GetMapping("/medica/reingresos")
    public List<ReporteDTOs.Conteo> reingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.reingresos(desde, hasta);
    }

    @GetMapping("/medica/tiempo-atencion")
    public List<ReporteDTOs.Conteo> tiempoAtencion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.tiempoAtencion(desde, hasta);
    }

    @GetMapping("/enfermeria/signos-fuera-rango")
    public List<ReporteDTOs.Conteo> signosFueraRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.signosFueraRango(desde, hasta);
    }

    @GetMapping("/enfermeria/eventos-adversos")
    public List<ReporteDTOs.Conteo> eventosAdversos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.eventosAdversos(desde, hasta);
    }

    @GetMapping("/enfermeria/carga-enfermero")
    public List<ReporteDTOs.Conteo> cargaPorEnfermero(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.cargaPorEnfermero(desde, hasta);
    }

    @GetMapping("/usuarios/sesiones-activas")
    public List<ReporteDTOs.SesionActiva> sesionesActivas() {
        return reporteService.sesionesActivas();
    }

    @GetMapping("/usuarios/permisos-por-modulo")
    public List<ReporteDTOs.PermisoModulo> permisosPorModulo() {
        return reporteService.permisosPorModulo();
    }

    @GetMapping("/usuarios/intentos-fallidos")
    public List<ReporteDTOs.Conteo> intentosFallidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.intentosFallidos(desde, hasta);
    }

    @GetMapping("/usuarios/actividad")
    public List<ReporteDTOs.Conteo> actividadPorUsuario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.actividadPorUsuario(desde, hasta);
    }

    @GetMapping("/usuarios/creados-por-periodo")
    public List<ReporteDTOs.Conteo> usuariosCreados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.usuariosCreados(desde, hasta);
    }
}
