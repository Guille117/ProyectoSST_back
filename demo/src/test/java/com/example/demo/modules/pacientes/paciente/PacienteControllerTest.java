package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private PacienteService service;
    @MockitoBean private com.example.demo.security.JwtUtil jwtUtil;
    @MockitoBean private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crearDevuelveIdentificadoresDelExpedienteYEpisodio() throws Exception {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                "1234567890123", "Ana", "Lopez", LocalDate.of(1990, 1, 1), Sexo.FEMENINO,
                "12345678", null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona, null, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);
        when(service.crear(any())).thenReturn(new PacienteDTOs.Response(1L, 2L, 3L, java.time.LocalDateTime.now(), null, null, false));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.expedienteId").value(2))
                .andExpect(jsonPath("$.episodioId").value(3));
    }

    @Test
    void crearRechazaSolicitudSinDatosDePaciente() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content("{\"episodio\":{\"tipoAtencion\":\"EMERGENCIA\",\"descripcion\":\"Ingreso\",\"medicoId\":5}}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void crearPropagaComoBadRequestLaReglaDeResponsable() throws Exception {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                "1234567890123", "Ana", "Lopez", LocalDate.of(2015, 1, 1), Sexo.FEMENINO,
                null, null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona, null, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);
        when(service.crear(any())).thenThrow(new IllegalArgumentException("El responsable es obligatorio"));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El responsable es obligatorio"));
    }

    @Test
    void crearPermiteCuiYTelefonoAusentesParaPaciente() throws Exception {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                null, "Ana", "Lopez", LocalDate.of(1990, 1, 1), Sexo.FEMENINO, null, null);
        PacienteDTOs.Request request = new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona, null, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                5L);
        when(service.crear(any())).thenReturn(new PacienteDTOs.Response(1L, 2L, 3L, java.time.LocalDateTime.now(), null, null, false));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void crearMultipartAceptaArchivoDeReferenciaOpcional() throws Exception {
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(requestConReferencia()));
        when(service.crear(any(PacienteDTOs.Request.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new PacienteDTOs.Response(1L, 2L, 3L, java.time.LocalDateTime.now(), null, 60L, false));

        mockMvc.perform(multipart("/api/v1/pacientes").file(requestPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenciaId").value(60));

        verify(service).crear(any(PacienteDTOs.Request.class), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void crearMultipartConArchivoPasaElArchivoAlServicio() throws Exception {
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(requestConReferencia()));
        MockMultipartFile archivo = new MockMultipartFile(
                "archivoReferencia", "origen.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());
        when(service.crear(any(PacienteDTOs.Request.class), any(MultipartFile.class),
                isNull(), isNull(), isNull()))
                .thenReturn(new PacienteDTOs.Response(1L, 2L, 3L, java.time.LocalDateTime.now(), null, 60L, false));

        mockMvc.perform(multipart("/api/v1/pacientes").file(requestPart).file(archivo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenciaId").value(60));

        verify(service).crear(any(PacienteDTOs.Request.class), any(MultipartFile.class),
                isNull(), isNull(), isNull());
    }

    private PacienteDTOs.Request requestConReferencia() {
        PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                "1234567890123", "Ana", "Lopez", LocalDate.of(1990, 1, 1), Sexo.FEMENINO,
                "12345678", null);
        return new PacienteDTOs.Request(
                new PacienteDTOs.PacienteRequest(null, persona, null, null, null, null),
                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null,
                        new PacienteDTOs.ReferenciaRequest(8L, "Evaluacion")),
                5L);
    }

        @Test
        void listar_delegaEstadoOpcionalAlServicio() throws Exception {
                when(service.listar(null)).thenReturn(java.util.List.of(
                                new PacienteDTOs.ListadoResponse(20L, "EXP-30", 42L, 42L, java.time.LocalDateTime.now(), "Ana Maria Lopez Ruiz", "1234567890123", Sexo.FEMENINO, "12345678",
                                                TipoAtencion.HOSPITALIZACION, true)));

                mockMvc.perform(get("/api/v1/pacientes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].pacienteId").value(20))
                                .andExpect(jsonPath("$[0].codigoExpediente").value("EXP-30"))
                                .andExpect(jsonPath("$[0].episodioId").value(42))
                                .andExpect(jsonPath("$[0].ultimoEpisodioId").value(42))
                                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Maria Lopez Ruiz"))
                                .andExpect(jsonPath("$[0].cui").value("1234567890123"))
                                .andExpect(jsonPath("$[0].sexo").value("FEMENINO"))
                                .andExpect(jsonPath("$[0].telefono").value("12345678"))
                                .andExpect(jsonPath("$[0].tipoTratamiento").value("HOSPITALIZACION"))
                                .andExpect(jsonPath("$[0].estado").value(true));
        }

        @Test
        void buscar_devuelvePacientesPorTexto() throws Exception {
                when(service.buscar("ana")).thenReturn(java.util.List.of(
                                new PacienteDTOs.ListadoResponse(20L, "EXP-30", 42L, 42L, java.time.LocalDateTime.now(),
                                                "Ana Maria Lopez Ruiz", "1234567890123", Sexo.FEMENINO, "12345678",
                                                TipoAtencion.EMERGENCIA, true)));

                mockMvc.perform(get("/api/v1/pacientes/buscar").param("texto", "ana"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].pacienteId").value(20))
                                .andExpect(jsonPath("$[0].cui").value("1234567890123"))
                                .andExpect(jsonPath("$[0].sexo").value("FEMENINO"))
                                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Maria Lopez Ruiz"));
        }

        @Test
        void obtenerDetalle_devuelveLosDatosDelPaciente() throws Exception {
                when(service.obtenerDetalle(20L)).thenReturn(new PacienteDTOs.DetalleResponse(
                                20L, "EXP-30", "Ana Maria Lopez Ruiz", "Ana Maria", "Lopez Ruiz",
                                "1234567890123", LocalDate.of(1990, 1, 1), 36, Sexo.FEMENINO,
                                "12345678", null, EstadoCivil.SOLTERO, null, null, true,
                                42L, TipoAtencion.EMERGENCIA, "Ingreso", java.time.LocalDateTime.now(),
                                5L, "Dr. Juan Perez", null, null, null, null));

                mockMvc.perform(get("/api/v1/pacientes/20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.pacienteId").value(20))
                                .andExpect(jsonPath("$.codigoExpediente").value("EXP-30"))
                                .andExpect(jsonPath("$.nombres").value("Ana Maria"))
                                .andExpect(jsonPath("$.apellidos").value("Lopez Ruiz"))
                                .andExpect(jsonPath("$.edad").value(36))
                                .andExpect(jsonPath("$.sexo").value("FEMENINO"))
                                .andExpect(jsonPath("$.episodioId").value(42))
                                .andExpect(jsonPath("$.medicoNombre").value("Dr. Juan Perez"));
        }

        @Test
        void actualizar_devuelveLosIdentificadoresActualizados() throws Exception {
                PacienteDTOs.PersonaRequest persona = new PacienteDTOs.PersonaRequest(
                                "1234567890123", "Ana", "Lopez", LocalDate.of(1990, 1, 1), Sexo.FEMENINO,
                                "12345678", null);
                PacienteDTOs.ActualizacionRequest request = new PacienteDTOs.ActualizacionRequest(
                                new PacienteDTOs.PacienteRequest(null, persona, null, null, null, null),
                                new PacienteDTOs.EpisodioRequest(TipoAtencion.EMERGENCIA, "Ingreso", 5L, null, null),
                                5L);
                MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                                objectMapper.writeValueAsBytes(request));
                when(service.actualizar(eq(20L), any(), isNull()))
                                .thenReturn(new PacienteDTOs.Response(20L, 30L, 42L, java.time.LocalDateTime.now(),
                                                null, null, true));

                mockMvc.perform(multipart("/api/v1/pacientes/20")
                                .file(requestPart)
                                .with(accion -> {
                                        accion.setMethod("PUT");
                                        return accion;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.pacienteId").value(20))
                                .andExpect(jsonPath("$.expedienteId").value(30))
                                .andExpect(jsonPath("$.episodioId").value(42));
        }
}