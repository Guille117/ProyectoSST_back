package com.example.demo.modules.usuarios.configuracion;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.Sexo;
import com.example.demo.modules.usuarios.usuarios.UsuarioDTOs;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioMapper;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.security.UsuarioActualService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long PERSONA_ID = 10L;

    @Mock
    private UsuarioActualService usuarioActualService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ConfiguracionService service;

    private PersonaEntity persona() {
        return PersonaEntity.builder()
                .id(PERSONA_ID)
                .cui("1234567890123")
                .sexo(Sexo.MASCULINO)
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("12345678")
                .email("juan@test.com")
                .build();
    }

    private UsuarioEntity usuario(String username, String password) {
        return UsuarioEntity.builder()
                .id(USUARIO_ID)
                .codigo("USR-01")
                .username(username)
                .password(password)
                .persona(persona())
                .estado(true)
                .build();
    }

    private UsuarioDTOs.PersonaRequest personaRequest(String cui) {
        return new UsuarioDTOs.PersonaRequest(
                cui,
                "Juan Carlos",
                "Perez",
                Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1),
                "87654321",
                "nuevo@test.com");
    }

    private UsuarioDTOs.Response respuesta() {
        return new UsuarioDTOs.Response(
                USUARIO_ID,
                "USR-01",
                "jperez",
                "Juan Carlos",
                "Perez",
                "1234567890123",
                Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1),
                "87654321",
                "nuevo@test.com",
                new UsuarioDTOs.PuestoResponse(1L, "Director"),
                new UsuarioDTOs.HorarioResponse(1L, "Diurno"),
                List.of(1L),
                List.of("Administrador"),
                true,
                null);
    }

    @Test
    void obtenerPerfil_deberiaRetornarElPerfilDelUsuarioAutenticado() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        UsuarioDTOs.Response esperado = respuesta();
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(esperado);

        UsuarioDTOs.Response resultado = service.obtenerPerfil();

        assertSame(esperado, resultado);
    }

    @Test
    void obtenerPerfil_deberiaFallarSiNoHayUsuarioAutenticado() {
        when(usuarioActualService.actual()).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.obtenerPerfil());
    }

    @Test
    void actualizarPerfil_deberiaActualizarLosDatosPersonales() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        UsuarioDTOs.Response esperado = respuesta();
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(personaRepository.findByCui("9876543210987")).thenReturn(Optional.empty());
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toDTO(usuario)).thenReturn(esperado);

        UsuarioDTOs.Response resultado = service.actualizarPerfil(personaRequest("9876543210987"));

        assertSame(esperado, resultado);
        assertEquals("9876543210987", usuario.getPersona().getCui());
        assertEquals("Juan Carlos", usuario.getPersona().getNombres());
        assertEquals("Perez", usuario.getPersona().getApellidos());
        assertEquals("87654321", usuario.getPersona().getTelefono());
        assertEquals("nuevo@test.com", usuario.getPersona().getEmail());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarPerfil_deberiaRechazarCuiDeOtroUsuario() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        PersonaEntity otraPersona = PersonaEntity.builder().id(99L).cui("9876543210987").build();
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(personaRepository.findByCui("9876543210987")).thenReturn(Optional.of(otraPersona));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarPerfil(personaRequest("9876543210987")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfil_deberiaPermitirConservarElMismoCui() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(usuario.getPersona()));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toDTO(usuario)).thenReturn(respuesta());

        service.actualizarPerfil(personaRequest("1234567890123"));

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarUsuario_deberiaActualizarYGenerarUnTokenNuevo() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByUsernameIgnoreCase("jperez2")).thenReturn(Optional.empty());
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(jwtUtil.generateToken("jperez2", USUARIO_ID)).thenReturn("token-nuevo");

        ConfiguracionDTOs.CambioUsuarioResponse resultado =
                service.cambiarUsuario(new ConfiguracionDTOs.CambioUsuarioRequest("jperez2"));

        assertEquals("token-nuevo", resultado.token());
        assertEquals("jperez2", resultado.username());
        assertEquals("jperez2", usuario.getUsername());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarUsuario_deberiaRechazarUsernameDuplicado() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        UsuarioEntity otroUsuario = usuario("jperez2", "hash");
        otroUsuario.setId(2L);
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByUsernameIgnoreCase("jperez2")).thenReturn(Optional.of(otroUsuario));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarUsuario(new ConfiguracionDTOs.CambioUsuarioRequest("jperez2")));

        verify(usuarioRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void cambiarUsuario_deberiaPermitirConservarElMismoUsername() {
        UsuarioEntity usuario = usuario("jperez", "hash");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(jwtUtil.generateToken("jperez", USUARIO_ID)).thenReturn("token");

        service.cambiarUsuario(new ConfiguracionDTOs.CambioUsuarioRequest("jperez"));

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarUsuario_deberiaRechazarAlUsuarioDelSistema() {
        UsuarioEntity usuario = usuario("guille117", "hash");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarUsuario(new ConfiguracionDTOs.CambioUsuarioRequest("otro")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_deberiaActualizarLaContrasena() {
        UsuarioEntity usuario = usuario("jperez", "hash-actual");
        ConfiguracionDTOs.CambioPasswordRequest request =
                new ConfiguracionDTOs.CambioPasswordRequest("actual123", "nueva123", "nueva123");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual123", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        service.cambiarPassword(request);

        assertEquals("hash-nuevo", usuario.getPassword());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPassword_deberiaRechazarPasswordActualIncorrecta() {
        UsuarioEntity usuario = usuario("jperez", "hash-actual");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-actual")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarPassword(
                new ConfiguracionDTOs.CambioPasswordRequest("incorrecta", "nueva123", "nueva123")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_deberiaRechazarConfirmacionDistinta() {
        UsuarioEntity usuario = usuario("jperez", "hash-actual");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual123", "hash-actual")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarPassword(
                new ConfiguracionDTOs.CambioPasswordRequest("actual123", "nueva123", "otra123")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_deberiaRechazarLaMismaContrasena() {
        UsuarioEntity usuario = usuario("jperez", "hash-actual");
        when(usuarioActualService.actual()).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual123", "hash-actual")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarPassword(
                new ConfiguracionDTOs.CambioPasswordRequest("actual123", "actual123", "actual123")));

        verify(usuarioRepository, never()).save(any());
    }
}
