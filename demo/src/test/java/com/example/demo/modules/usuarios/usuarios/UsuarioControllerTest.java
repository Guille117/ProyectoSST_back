package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.puestoController;
import com.example.demo.modules.usuarios.puesto.puestoService;
import com.example.demo.modules.usuarios.especialidad.especialidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest({UsuarioController.class, puestoController.class})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private puestoService puestoService;

        @MockitoBean
        private especialidadService especialidadService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

    @MockitoBean
    private com.example.demo.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.demo.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void crear_DeberiaRetornarCreated() throws Exception {
        UsuarioDTOs.Request request = new UsuarioDTOs.Request(
                new UsuarioDTOs.PersonaRequest("1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", 1L, true
        );

        when(usuarioService.crear(any(UsuarioDTOs.Request.class))).thenReturn("123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("123456"));
    }

    @Test
    void crear_DeberiaRetornarBadRequest_CuandoCuiInvalido() throws Exception {
        UsuarioDTOs.Request request = new UsuarioDTOs.Request(
                new UsuarioDTOs.PersonaRequest("123", "Juan", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", true
        );

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

                @Test
                void crear_AceptaIdDeEspecialidadConLaClaveEspecialidad() throws Exception {
                                when(usuarioService.crear(any(UsuarioDTOs.Request.class))).thenReturn("123456");

                                mockMvc.perform(post("/api/v1/usuarios")
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "persona": {
                                                                                                                                                "cui": "1234567890123",
                                                                                                                                                "nombres": "Juan",
                                                                                                                                                "apellidos": "Perez",
                                                                                                                                                "sexo": "MASCULINO",
                                                                                                                                                "fechaNacimiento": "1990-01-01",
                                                                                                                                                "telefono": "12345678",
                                                                                                                                                "email": "juan@test.com"
                                                                                                                                        },
                                                                                                                                        "puestoId": 1,
                                                                                                                                        "horarioId": 1,
                                                                                                                                        "rolIds": [1],
                                                                                                                                        "username": "jperez",
                                                                                                                                        "especialidad": 7
                                                                                                                                }
                                                                                                                                """))
                                                                .andExpect(status().isCreated());

                                org.mockito.ArgumentCaptor<UsuarioDTOs.Request> requestCaptor =
                                                                org.mockito.ArgumentCaptor.forClass(UsuarioDTOs.Request.class);
                                verify(usuarioService).crear(requestCaptor.capture());
                                org.junit.jupiter.api.Assertions.assertEquals(7L, requestCaptor.getValue().especialidadId());
                }

    @Test
    void obtenerPorId_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "jperez", "Juan", "Perez", "1234567890123", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                new UsuarioDTOs.PuestoResponse(1L, "Director"),
                new UsuarioDTOs.HorarioResponse(1L, "Diurno"),
                List.of(1L), List.of("Admin"), true,
                new UsuarioDTOs.EspecialidadResponse(7L, "Cardiología")
        );

        when(usuarioService.obtenerPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jperez"))
                .andExpect(jsonPath("$.especialidad.id").value(7))
                .andExpect(jsonPath("$.especialidad.nombre").value("Cardiología"));
    }

    @Test
    void actualizar_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.UpdateRequest request = new UsuarioDTOs.UpdateRequest(
                new UsuarioDTOs.PersonaRequest("1234567890123", "Juan Carlos", "Perez", Sexo.MASCULINO,
                        LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"),
                1L, 1L, List.of(1L), "jperez", null, null, 1L, true
        );

        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan Carlos", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.actualizar(eq(1L), any(UsuarioDTOs.UpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"));
    }

    @Test
    void cambiarEstado_DeberiaRetornarOk() throws Exception {
        mockMvc.perform(patch("/api/v1/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.ListResponse response = new UsuarioDTOs.ListResponse(
                1L, "USR-01", "Juan Perez", "12345678", List.of("Admin"), true,
                new UsuarioDTOs.EspecialidadResponse(1L, "Cardiología")
        );

        when(usuarioService.obtenerTodos(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("USR-01"))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Juan Perez"))
                .andExpect(jsonPath("$[0].roles[0]").value("Admin"))
                .andExpect(jsonPath("$[0].especialidad.id").value(1))
                .andExpect(jsonPath("$[0].especialidad.nombre").value("Cardiología"));
    }

    @Test
    void obtenerMedicosActivos_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.MedicoResponse response = new UsuarioDTOs.MedicoResponse(5L, "Ana Lopez", "Cardiología");
        when(usuarioService.obtenerMedicosActivos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Lopez"))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiología"))
                .andExpect(jsonPath("$[0].codigo").doesNotExist())
                .andExpect(jsonPath("$[0].telefono").doesNotExist())
                .andExpect(jsonPath("$[0].roles").doesNotExist())
                .andExpect(jsonPath("$[0].estado").doesNotExist());
    }

        @Test
        void contarCatalogos_DeberiaRetornarPuestosYEspecialidadesActivos() throws Exception {
                when(puestoService.contarPuestos()).thenReturn(5L);
                when(especialidadService.contarEspecialidades()).thenReturn(3L);

                mockMvc.perform(get("/api/v1/puestos/conteo"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.puestos").value(5))
                                .andExpect(jsonPath("$.especialidades").value(3));
        }

    @Test
    void buscar_DeberiaRetornarOk() throws Exception {
        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "Director",
                1L, "HOR-01", "Diurno",
                List.of(new UsuarioDTOs.RolResponse(1L, "ROL-01", "Admin")),
                "jperez", true
        );

        when(usuarioService.buscarPorCriterio(eq("Juan"), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios/buscar").param("criterio", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }
}
