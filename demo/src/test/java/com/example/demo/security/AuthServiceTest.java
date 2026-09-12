package com.example.demo.security;

import com.example.demo.modules.usuarios.usuarios.*;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.horarios.DiaSemana;
import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioSemanalDetalleEntity;
import com.example.demo.modules.usuarios.roles.ModuloEntity;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolPermisoEntity;
import com.example.demo.modules.usuarios.roles.SubmoduloEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private com.example.demo.modules.usuarios.usuarios.UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioPinRepository pinRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private UsuarioEntity usuarioSemanal;
    private UsuarioEntity usuarioRotativo;
    private UsuarioEntity usuarioAdmin;

    @BeforeEach
    void setUp() {
        // Horario semanal LUNES 08:00-17:00 activo
        HorarioEntity horarioSemanal = HorarioEntity.builder()
                .id(1L).codigo("HOR-01").nombre("Diurno").esRotativo(false).estado(true)
                .build();
        HorarioSemanalDetalleEntity detalleLunes = HorarioSemanalDetalleEntity.builder()
                .id(10L).horario(horarioSemanal).diaSemana(DiaSemana.LUNES)
                .horaEntrada(LocalTime.of(8, 0)).horaSalida(LocalTime.of(17, 0)).activo(true)
                .build();
        horarioSemanal.setSemanalDetalles(List.of(detalleLunes));
        horarioSemanal.setTurnoDetalle(null);

        // Horario rotativo
        HorarioEntity horarioRotativo = HorarioEntity.builder()
                .id(2L).codigo("HOR-02").nombre("Rotativo").esRotativo(true).estado(true)
                .build();

        PersonaEntity persona = PersonaEntity.builder()
                .id(1L).cui("1234567890123").nombres("Juan").apellidos("Perez")
                .sexo(Sexo.MASCULINO).fechaNacimiento(LocalDate.of(1990,1,1))
                .build();

        puestoEntity puestoDoctor = new puestoEntity();
        puestoDoctor.setId(1L); puestoDoctor.setNombre("Doctor"); puestoDoctor.setEstado(true);
        puestoEntity puestoAdmin = new puestoEntity();
        puestoAdmin.setId(5L); puestoAdmin.setNombre("Administrador"); puestoAdmin.setEstado(true);
        puestoEntity puestoDirector = new puestoEntity();
        puestoDirector.setId(1L); puestoDirector.setNombre("Director"); puestoDirector.setEstado(true);

        ModuloEntity modulo = ModuloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").estado(true).build();
        SubmoduloEntity sub = SubmoduloEntity.builder().id(1L).modulo(modulo).codigo("ROLES").nombre("Roles").estado(true).build();
        RolPermisoEntity permiso = RolPermisoEntity.builder().id(1L).submodulo(sub).puedeLeer(true).puedeCrear(true).puedeEditar(false).puedeEliminar(false).build();

        RolEntity rolNormal = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Doctor").estado(true).permisos(List.of(permiso)).build();
        permiso.setRol(rolNormal);

        RolEntity rolAdmin = RolEntity.builder().id(2L).codigo("ROL-02").nombre("ADMINISTRADOR").estado(true).permisos(List.of(permiso)).build();

        usuarioSemanal = UsuarioEntity.builder()
                .id(1L).codigo("USR-01").username("jperez").password("encodedPass").estado(true)
                .persona(persona).puesto(puestoDoctor).horario(horarioSemanal).rol(rolNormal)
                .build();

        usuarioRotativo = UsuarioEntity.builder()
                .id(2L).codigo("USR-02").username("rotativo").password("encodedPass").estado(true)
                .persona(persona).puesto(puestoDoctor).horario(horarioRotativo).rol(rolNormal)
                .build();

        usuarioAdmin = UsuarioEntity.builder()
                .id(3L).codigo("USR-03").username("admin").password("encodedPass").estado(true)
                .persona(persona).puesto(puestoAdmin).horario(horarioSemanal).rol(rolAdmin)
                .build();
    }

    @Test
    void login_Exitoso_DentroDeHorario() {
        // Given LUNES 10:00 dentro de 08:00-17:00
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("jperez", 1L)).thenReturn("jwt-token");

        LocalDate lunes = LocalDate.of(2026, 8, 24); // Lunes
        LocalTime horaDentro = LocalTime.of(10, 0);

        // When
        AuthDTOs.AuthResponse resp = authService.login(req, lunes, horaDentro);

        // Then
        assertEquals("jwt-token", resp.token());
        assertEquals("Bearer", resp.tipo());
        assertEquals("jperez", resp.username());
        assertEquals("Juan Perez", resp.nombreCompleto());
        assertNotNull(resp.permisos());
        verify(jwtUtil).generateToken("jperez", 1L);
    }

    @Test
    void login_LanzaExcepcion_CuandoCredencialesInvalidas() {
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "wrong");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(req, LocalDate.now(), LocalTime.now()));
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void login_Rechazo_PorFueraDeHorario() {
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);

        // LUNES 20:00 fuera de rango, o MARTES (no configurado)
        LocalDate lunes = LocalDate.of(2026, 8, 24);
        LocalTime horaFuera = LocalTime.of(20, 0);

        HorarioAccessException ex = assertThrows(HorarioAccessException.class, () -> authService.login(req, lunes, horaFuera));
        assertEquals("Acceso denegado: Fuera del horario de trabajo asignado", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void login_Rechazo_PorDiaNoConfigurado() {
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);

        LocalDate martes = LocalDate.of(2026, 8, 25); // Martes no tiene detalle
        LocalTime hora = LocalTime.of(10, 0);

        assertThrows(HorarioAccessException.class, () -> authService.login(req, martes, hora));
    }

    @Test
    void login_DeberiaPermitir_Administrador_FueraDeHorario() {
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("admin", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("admin", 3L)).thenReturn("admin-token");

        // Fuera de horario pero es ADMINISTRADOR -> debe pasar
        LocalDate lunes = LocalDate.of(2026, 8, 24);
        LocalTime horaFuera = LocalTime.of(23, 0);

        AuthDTOs.AuthResponse resp = authService.login(req, lunes, horaFuera);

        assertEquals("admin-token", resp.token());
        assertEquals("ADMINISTRADOR", resp.rol());
    }

    @Test
    void login_DeberiaPermitir_Rotativo_SinValidacionHoraria() {
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("rotativo", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("rotativo")).thenReturn(Optional.of(usuarioRotativo));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("rotativo", 2L)).thenReturn("rotativo-token");

        AuthDTOs.AuthResponse resp = authService.login(req, LocalDate.now(), LocalTime.now());

        assertEquals("rotativo-token", resp.token());
    }

    @Test
    void login_LanzaExcepcion_CuandoUsuarioInactivo() {
        usuarioSemanal.setEstado(false);
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.login(req, LocalDate.now(), LocalTime.now()));
    }

    @Test
    void login_LanzaExcepcion_CuandoPasswordAunNoEstablecida() {
        usuarioSemanal.setPassword(null);
        AuthDTOs.LoginRequest req = new AuthDTOs.LoginRequest("jperez", "password123");
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));

        assertThrows(BadCredentialsException.class, () -> authService.login(req, LocalDate.now(), LocalTime.now()));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void establecerCredenciales_Exitoso() {
        usuarioSemanal.setPassword(null);
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
                .id(1L).usuario(usuarioSemanal).codigo("123456")
                .fechaCreacion(LocalDateTime.now()).fechaExpiracion(LocalDateTime.now().plusHours(1))
                .usado(false).intentos(0).build();

        AuthDTOs.EstablecerCredencialesRequest req = new AuthDTOs.EstablecerCredencialesRequest("jperez", "123456", "nuevaPass123");

        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(pinRepository.findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(1L)).thenReturn(Optional.of(pin));
        when(passwordEncoder.encode("nuevaPass123")).thenReturn("encodedNueva");
        when(jwtUtil.generateToken("jperez", 1L)).thenReturn("jwt-token");

        AuthDTOs.AuthResponse resp = authService.establecerCredenciales(req);

        assertEquals("jwt-token", resp.token());
        assertEquals("encodedNueva", usuarioSemanal.getPassword());
        assertTrue(pin.isUsado());
        verify(pinRepository, atLeastOnce()).save(pin);
    }

    @Test
    void establecerCredenciales_LanzaExcepcion_CuandoPinIncorrecto() {
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
                .id(1L).usuario(usuarioSemanal).codigo("123456")
                .fechaCreacion(LocalDateTime.now()).fechaExpiracion(LocalDateTime.now().plusHours(1))
                .usado(false).intentos(0).build();

        AuthDTOs.EstablecerCredencialesRequest req = new AuthDTOs.EstablecerCredencialesRequest("jperez", "000000", "nuevaPass123");

        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(pinRepository.findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(1L)).thenReturn(Optional.of(pin));

        assertThrows(IllegalArgumentException.class, () -> authService.establecerCredenciales(req));
        assertEquals(1, pin.getIntentos());
    }

    @Test
    void establecerCredenciales_LanzaExcepcion_CuandoPinExpirado() {
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
                .id(1L).usuario(usuarioSemanal).codigo("123456")
                .fechaCreacion(LocalDateTime.now().minusHours(2)).fechaExpiracion(LocalDateTime.now().minusHours(1))
                .usado(false).intentos(0).build();

        AuthDTOs.EstablecerCredencialesRequest req = new AuthDTOs.EstablecerCredencialesRequest("jperez", "123456", "nuevaPass123");

        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(pinRepository.findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(1L)).thenReturn(Optional.of(pin));

        assertThrows(IllegalArgumentException.class, () -> authService.establecerCredenciales(req));
        assertTrue(pin.isUsado());
    }

    @Test
    void establecerCredenciales_LanzaExcepcion_CuandoLimiteDeIntentosAlcanzado() {
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
                .id(1L).usuario(usuarioSemanal).codigo("123456")
                .fechaCreacion(LocalDateTime.now()).fechaExpiracion(LocalDateTime.now().plusHours(1))
                .usado(false).intentos(3).build();

        AuthDTOs.EstablecerCredencialesRequest req = new AuthDTOs.EstablecerCredencialesRequest("jperez", "123456", "nuevaPass123");

        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuarioSemanal));
        when(pinRepository.findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(1L)).thenReturn(Optional.of(pin));

        assertThrows(IllegalArgumentException.class, () -> authService.establecerCredenciales(req));
        verify(pinRepository, never()).save(any());
    }
}
