package com.example.demo.modules.farmacia.medicamentoLog;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(medicamentoLogController.class)
class medicamentoLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private medicamentoLogService service;

    @Test
    void crear_debeAceptarDosisNumerica() throws Exception {
        medicamentoLogDTOs.Request request = new medicamentoLogDTOs.Request(
                "Paracetamol", new BigDecimal("500.5"), 1L, 1L, 1L, 1L);
        medicamentoLogDTOs.Response response = new medicamentoLogDTOs.Response(
                1L, "Paracetamol", "MED-1", new BigDecimal("500.5"), 1L, "mg", 1L,
                "Generica", 1L, "Oral", 1L, "Tableta");
        when(service.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/medicamentoLog")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_debeRechazarDosisNoNumerica() throws Exception {
        String json = "{\"nombre\":\"Paracetamol\",\"dosis\":\"quinientos\",\"unidadMedidaId\":1,\"marcaId\":1,\"viaAdminId\":1,\"presentacionId\":1}";

        mockMvc.perform(post("/api/v1/medicamentoLog")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodos_debeRetornarFormatoDeListadoSolicitado() throws Exception {
        when(service.obtenerTodos(true)).thenReturn(java.util.List.of(
                new medicamentoLogDTOs.MedicamentoLogResponse(
                        1L, "Amoxicilina", "MED-1", new BigDecimal("500"), "Marca A", "Tableta", "Oral", "mg", true)));

        mockMvc.perform(get("/api/v1/medicamentoLog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Amoxicilina"))
                .andExpect(jsonPath("$[0].dosis").value(500))
                .andExpect(jsonPath("$[0].marca").value("Marca A"))
                .andExpect(jsonPath("$[0].presentacion").value("Tableta"))
                .andExpect(jsonPath("$[0].viaAdministracion").value("Oral"))
                .andExpect(jsonPath("$[0].unidadMedida").value("mg"))
                .andExpect(jsonPath("$[0].estado").value(true))
                .andExpect(jsonPath("$[0].viaAdmin").doesNotExist());
    }

    @Test
    void buscar_debeRetornarDatosPorIdParaRequest() throws Exception {
        when(service.buscarPorId(7L, true)).thenReturn(
                new medicamentoLogDTOs.MedicamentoLogBusquedaResponse(
                        "Amoxicilina", new BigDecimal("500"), 3L, 4L, 5L, 6L));

        mockMvc.perform(get("/api/v1/medicamentoLog/buscar/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Amoxicilina"))
                .andExpect(jsonPath("$.dosis").value(500))
                .andExpect(jsonPath("$.unidadMedidaId").value(3))
                .andExpect(jsonPath("$.viaAdminId").value(4))
                .andExpect(jsonPath("$.presentacionId").value(5))
                .andExpect(jsonPath("$.marcaId").value(6));
    }

    @Test
    void buscar_debeAceptarFiltrosDeNombreYCatalogos() throws Exception {
        when(service.buscar("amox", 2L, 3L, 4L, true)).thenReturn(java.util.List.of(
                new medicamentoLogDTOs.MedicamentoLogResponse(
                        7L, "Amoxicilina", "MED-7", new BigDecimal("500"), "Marca A", "Tableta", "Oral", "mg", true)));

        mockMvc.perform(get("/api/v1/medicamentoLog/buscar")
                        .param("nombre", "amox")
                        .param("marcaId", "2")
                        .param("presentacionId", "3")
                        .param("viaAdminId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Amoxicilina"))
                .andExpect(jsonPath("$[0].dosis").value(500))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].marca").value("Marca A"))
                .andExpect(jsonPath("$[0].presentacion").value("Tableta"))
                .andExpect(jsonPath("$[0].viaAdministracion").value("Oral"))
                .andExpect(jsonPath("$[0].unidadMedida").value("mg"))
                .andExpect(jsonPath("$[0].estado").value(true));
    }

    @Test
    void obtenerPorId_debeUsarActivoTruePorDefecto() throws Exception {
        when(service.obtenerPorId(7L, true)).thenReturn(new medicamentoLogDTOs.Response(
                7L, "Amoxicilina", "MED-7", new BigDecimal("500"), 3L, "mg", 6L,
                "Marca A", 4L, "Oral", 5L, "Tableta"));

        mockMvc.perform(get("/api/v1/medicamentoLog/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

        @Test
        void cambiarEstado_debeRetornarOk() throws Exception {
                mockMvc.perform(patch("/api/v1/medicamentoLog/7"))
                                .andExpect(status().isOk());
        }

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;
}