package com.example.demo.modules.farmacia.compras;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraService service;

    private final MockMultipartFile comprobante = new MockMultipartFile(
            "comprobante", "factura.pdf", "application/pdf", "%PDF-1.4 contenido".getBytes());

    @Test
    void crear_debeRetornarCreatedCuandoComprobanteValido() throws Exception {
        CompraDTOs.Response resp = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, "uploads/comprobantes/compra-1-abc.pdf", true,
                1L, "Proveedor SA", LocalDateTime.now(), null);
        when(service.crear(any(), isNull())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/compras").file(comprobante))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRetornarCreatedSinComprobante() throws Exception {
        CompraDTOs.Response resp = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.ZERO, null, true,
                1L, "Proveedor SA", LocalDateTime.now(), null);
        when(service.crear(isNull(), isNull())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/compras")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    void obtenerPorId_debeRetornarOk() throws Exception {
        CompraDTOs.Response resp = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.TEN, "uploads/comprobantes/compra-1-abc.pdf", true,
                1L, "Proveedor SA", LocalDateTime.now(), null);
        when(service.obtenerPorId(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/compras/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_debeRetornarOk() throws Exception {
        when(service.obtenerTodos(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/compras"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_debeRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/compras/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerResumen_debeRetornarOkConCantidadDeProductos() throws Exception {
        when(service.obtenerResumen(any())).thenReturn(List.of(new CompraDTOs.ResumenResponse(
                1L, "COM-1", "Proveedor SA", LocalDateTime.now(), 15, BigDecimal.valueOf(150))));

        mockMvc.perform(get("/api/v1/compras/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("COM-1"))
                .andExpect(jsonPath("$[0].proveedor").value("Proveedor SA"))
                .andExpect(jsonPath("$[0].cantidadProductos").value(15))
                .andExpect(jsonPath("$[0].total").exists());
    }

    @Test
    void obtenerDetalle_debeRetornarOkConLotesDeMedicamentoEInsumo() throws Exception {
        CompraDTOs.DetalleResponse resp = new CompraDTOs.DetalleResponse(
                1L, "COM-1", 1L, "Proveedor SA", LocalDateTime.now(), BigDecimal.valueOf(150),
                List.of(
                        new CompraDTOs.LoteDetalleResponse(1L, "LOTE-A", "MED-0001", true, "Amoxicilina",
                                BigDecimal.valueOf(500), "mg", "Genfar", "Caja x 12", null, 10, BigDecimal.TEN, null),
                        new CompraDTOs.LoteDetalleResponse(2L, "LOTE-B", "INS-0001", false, "Gasa estéril",
                                null, null, "3M", null, "Gasa estéril 10x10 cm", 5, BigDecimal.valueOf(8), null)));
        when(service.obtenerDetalle(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/compras/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("COM-1"))
                .andExpect(jsonPath("$.proveedorNombre").value("Proveedor SA"))
                .andExpect(jsonPath("$.lotes[0].codigo").value("MED-0001"))
                .andExpect(jsonPath("$.lotes[0].esMedicamento").value(true))
                .andExpect(jsonPath("$.lotes[0].dosis").value(500))
                .andExpect(jsonPath("$.lotes[0].unidadMedida").value("mg"))
                .andExpect(jsonPath("$.lotes[0].fabricante").value("Genfar"))
                .andExpect(jsonPath("$.lotes[0].presentacion").value("Caja x 12"))
                .andExpect(jsonPath("$.lotes[1].codigo").value("INS-0001"))
                .andExpect(jsonPath("$.lotes[1].esMedicamento").value(false))
                .andExpect(jsonPath("$.lotes[1].fabricante").value("3M"))
                .andExpect(jsonPath("$.lotes[1].detalle").value("Gasa estéril 10x10 cm"));
    }

    @Test
    void actualizar_debeRetornarOkConComprobanteNuevo() throws Exception {
        CompraDTOs.Response resp = new CompraDTOs.Response(
                1L, "COM-1", BigDecimal.TEN, "uploads/comprobantes/compra-1-nuevo.pdf", true,
                1L, "Proveedor SA", LocalDateTime.now(), null);
        when(service.actualizar(eq(1L), any(), isNull())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/compras/1").file(comprobante).with(request -> {
            request.setMethod("PUT");
            return request;
        }))
                .andExpect(status().isOk());
    }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}
