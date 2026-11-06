package com.example.demo.modules.usuarios.configuracion;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.PersonaRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioDTOs;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioMapper;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioService;
import com.example.demo.security.JwtUtil;
import com.example.demo.security.UsuarioActualService;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Datos de la propia cuenta: perfil, nombre de usuario y contraseña. */
@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final UsuarioActualService usuarioActualService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public UsuarioDTOs.Response obtenerPerfil() {
        return usuarioMapper.toDTO(usuarioActual());
    }

    @Transactional
    public UsuarioDTOs.Response actualizarPerfil(UsuarioDTOs.PersonaRequest request) {
        UsuarioEntity usuario = usuarioActual();
        PersonaEntity persona = usuario.getPersona();

        String cui = StringNormalizer.normalizarTexto(request.cui());
        validarCuiDisponible(persona.getId(), cui);

        persona.setCui(cui);
        persona.asignarNombres(request.nombres(), request.apellidos());
        persona.setSexo(request.sexo());
        persona.setFechaNacimiento(request.fechaNacimiento());
        persona.setTelefono(StringNormalizer.normalizarNullable(request.telefono()));
        persona.setEmail(StringNormalizer.normalizarNullable(request.email()));

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public ConfiguracionDTOs.CambioUsuarioResponse cambiarUsuario(
            ConfiguracionDTOs.CambioUsuarioRequest request) {
        UsuarioEntity usuario = usuarioActual();

        if (UsuarioService.USUARIO_SISTEMA.equalsIgnoreCase(usuario.getUsername())) {
            throw new IllegalArgumentException("El usuario del sistema no se puede modificar.");
        }

        String username = StringNormalizer.normalizarTexto(request.username());
        validarUsernameDisponible(usuario.getId(), username);

        usuario.setUsername(username);
        usuarioRepository.save(usuario);

        return new ConfiguracionDTOs.CambioUsuarioResponse(
                jwtUtil.generateToken(username, usuario.getId()), username);
    }

    @Transactional
    public void cambiarPassword(ConfiguracionDTOs.CambioPasswordRequest request) {
        UsuarioEntity usuario = usuarioActual();

        if (usuario.getPassword() == null
                || !passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (!request.passwordNueva().equals(request.confirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        if (request.passwordNueva().equals(request.passwordActual())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    private UsuarioEntity usuarioActual() {
        return usuarioActualService.actual()
                .orElseThrow(() -> new AccessDeniedException("No autenticado"));
    }

    private void validarCuiDisponible(Long personaId, String cui) {
        personaRepository.findByCui(cui)
                .filter(persona -> !persona.getId().equals(personaId))
                .ifPresent(persona -> {
                    throw new IllegalArgumentException("Ya existe un usuario con el CUI: " + cui);
                });
    }

    private void validarUsernameDisponible(Long usuarioId, String username) {
        usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(usuario -> !usuario.getId().equals(usuarioId))
                .ifPresent(usuario -> {
                    throw new IllegalArgumentException(
                            "Ya existe un usuario con el username: " + username);
                });
    }
}
