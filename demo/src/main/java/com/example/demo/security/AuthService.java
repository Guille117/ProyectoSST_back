package com.example.demo.security;

import com.example.demo.modules.bitacora.BitacoraService;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioPinEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioPinRepository;
import com.example.demo.modules.usuarios.usuarios.NombrePersonaParser;
import com.example.demo.modules.usuarios.roles.RolEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom PIN_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPinRepository pinRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BitacoraService bitacoraService;
    private final HorarioLaboralService horarioLaboralService;

    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        LocalDateTime ahora = horarioLaboralService.momentoActual();
        AuthDTOs.AuthResponse response = login(request, ahora.toLocalDate(), ahora.toLocalTime());
        try {
            bitacoraService.registrarLogin(response.id(), response.username());
        } catch (Exception ignorada) {
            // La bitÃ¡cora no debe impedir el inicio de sesiÃ³n.
        }
        return response;
    }

    // Overload para testing con tiempo controlado
    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request, LocalDate fechaActual, LocalTime horaActual) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invÃ¡lidas"));

        // password null: el usuario aÃºn no completÃ³ el primer ingreso (establecer-credenciales).
        if (usuario.getPassword() == null || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invÃ¡lidas");
        }

        if (!usuario.isEstado()) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        horarioLaboralService.validarAcceso(usuario, LocalDateTime.of(fechaActual, horaActual));

        return construirAuthResponse(usuario);
    }

    /**
     * Registra el cierre de sesiÃ³n en la bitÃ¡cora. Nunca lanza errores hacia el
     * cliente: un fallo en la bitÃ¡cora no debe impedir salir de la sesiÃ³n.
     */
    public void logout(String username) {
        usuarioRepository.findByUsernameIgnoreCase(username).ifPresent(usuario -> {
            try {
                bitacoraService.registrarLogout(usuario.getId(), usuario.getUsername());
            } catch (Exception ignorada) {
                // La bitÃ¡cora no debe impedir el cierre de sesiÃ³n.
            }
        });
    }

    @Transactional(readOnly = true)
    public AuthDTOs.MeResponse me(String username) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return new AuthDTOs.MeResponse(usuario.getId(), usuario.getUsername(), usuario.isEstado());
    }

    private static final int MAX_INTENTOS_PIN = 3;

        @Transactional
        public AuthDTOs.CambioCredencialesPinResponse solicitarCambioCredenciales(
            AuthDTOs.SolicitudCambioCredencialesRequest request) {
        UsuarioEntity actor = usuarioRepository.findById(request.usuarioActualId())
            .orElseThrow(() -> new BadCredentialsException("Credenciales invÃ¡lidas"));

        if (!actor.isEstado() || actor.getPassword() == null
            || !passwordEncoder.matches(request.passwordActual(), actor.getPassword())) {
            throw new BadCredentialsException("Credenciales invÃ¡lidas");
        }

        UsuarioEntity objetivo = usuarioRepository.findById(request.usuarioId())
            .orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));

        for (UsuarioPinEntity pinAnterior : pinRepository.findByUsuarioIdAndUsadoFalse(objetivo.getId())) {
            pinAnterior.setUsado(true);
            pinRepository.save(pinAnterior);
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusHours(1);
        String codigo = String.format("%06d", PIN_RANDOM.nextInt(1_000_000));
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
            .usuario(objetivo)
            .codigo(codigo)
            .fechaCreacion(ahora)
            .fechaExpiracion(expiracion)
            .usado(false)
            .intentos(0)
            .build();
        pinRepository.save(pin);

        return new AuthDTOs.CambioCredencialesPinResponse(
            objetivo.getId(), objetivo.getUsername(), codigo, expiracion);
        }

    // Primer ingreso o restablecimiento de credenciales usando el PIN de un solo uso.
    @Transactional
    public AuthDTOs.AuthResponse establecerCredenciales(AuthDTOs.EstablecerCredencialesRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invÃ¡lidas"));

        UsuarioPinEntity pin = pinRepository.findFirstByUsuarioIdAndUsadoFalseOrderByIdDesc(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("No hay un PIN activo para este usuario. Solicite uno nuevo."));

        if (LocalDateTime.now().isAfter(pin.getFechaExpiracion())) {
            pin.setUsado(true);
            pinRepository.save(pin);
            throw new IllegalArgumentException("El PIN ha expirado. Solicite uno nuevo.");
        }

        if (pin.getIntentos() >= MAX_INTENTOS_PIN) {
            throw new IllegalArgumentException("Se alcanzÃ³ el lÃ­mite de intentos para este PIN. Solicite uno nuevo.");
        }

        if (!pin.getCodigo().equals(request.pin().trim())) {
            pin.setIntentos(pin.getIntentos() + 1);
            pinRepository.save(pin);
            throw new IllegalArgumentException("PIN incorrecto");
        }

        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuarioRepository.save(usuario);

        pin.setUsado(true);
        pin.setFechaUso(LocalDateTime.now());
        pinRepository.save(pin);

        horarioLaboralService.validarAcceso(usuario, horarioLaboralService.momentoActual());

        return construirAuthResponse(usuario);
    }

    private AuthDTOs.AuthResponse construirAuthResponse(UsuarioEntity usuario) {
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getId());

        String primerNombre = usuario.getPersona().getPrimerNombre();
        String primerApellido = usuario.getPersona().getPrimerApellido();
        if (primerNombre == null || primerNombre.isBlank() || primerApellido == null || primerApellido.isBlank()) {
            NombrePersonaParser.PartesNombre partes = NombrePersonaParser.separar(
                    usuario.getPersona().getNombres(), usuario.getPersona().getApellidos());
            if (primerNombre == null || primerNombre.isBlank()) {
                primerNombre = partes.primerNombre();
            }
            if (primerApellido == null || primerApellido.isBlank()) {
                primerApellido = partes.primerApellido();
            }
        }

        List<AuthDTOs.PermisoDTO> permisos = usuario.getRoles().stream()
            .flatMap(rol -> rol.getPermisos().stream())
                .map(p -> new AuthDTOs.PermisoDTO(
                        p.getSubmodulo().getId(),
                        p.getSubmodulo().getCodigo(),
                        p.getSubmodulo().getNombre(),
                        p.getSubmodulo().getModulo().getCodigo(),
                        p.getSubmodulo().getModulo().getNombre(),
                        p.isPuedeLeer(),
                        p.isPuedeCrear(),
                        p.isPuedeEditar(),
                        p.isPuedeEliminar()
                ))
                .toList();

        return new AuthDTOs.AuthResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getCodigo(),
                usuario.getUsername(),
            String.join(" ", java.util.stream.Stream.of(primerNombre, primerApellido)
                .filter(valor -> valor != null && !valor.isBlank())
                .toList()),
                usuario.getPuesto().getNombre(),
                usuario.getRoles().stream().map(RolEntity::getNombre).toList(),
                usuario.isEstado(),
                permisos
        );
    }

}
