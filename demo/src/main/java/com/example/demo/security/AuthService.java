package com.example.demo.security;

import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.modules.usuarios.horarios.DiaSemana;
import com.example.demo.modules.usuarios.horarios.HorarioSemanalDetalleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        return login(request, LocalDate.now(), LocalTime.now());
    }

    // Overload para testing con tiempo controlado
    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request, LocalDate fechaActual, LocalTime horaActual) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!usuario.isEstado()) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        // Validación de horario laboral
        validarHorarioLaboral(usuario, fechaActual, horaActual);

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getId());

        String nombreCompleto = usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidos();

        List<AuthDTOs.PermisoDTO> permisos = usuario.getRol().getPermisos().stream()
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
                nombreCompleto,
                usuario.getPuesto().getNombre(),
                usuario.getRol().getNombre(),
                usuario.isEstado(),
                permisos
        );
    }

    private void validarHorarioLaboral(UsuarioEntity usuario, LocalDate fechaActual, LocalTime horaActual) {
        // Bypass para ADMINISTRADOR o puesto Administrador / Director
        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
        String puestoNombre = usuario.getPuesto() != null ? usuario.getPuesto().getNombre() : "";

        if (rolNombre != null && rolNombre.equalsIgnoreCase("ADMINISTRADOR")) {
            return;
        }
        if (puestoNombre != null && (puestoNombre.equalsIgnoreCase("Administrador") || puestoNombre.equalsIgnoreCase("Director"))) {
            return;
        }

        var horario = usuario.getHorario();
        if (horario == null) {
            return;
        }

        if (horario.isEsRotativo()) {
            // Si es rotativo, permitir acceso según regla de turno configurada (actualmente irrestricto)
            return;
        }

        // Horario SEMANAL
        DiaSemana diaActual = mapDayOfWeek(fechaActual.getDayOfWeek());
        HorarioSemanalDetalleEntity detalle = horario.getSemanalDetalles().stream()
                .filter(d -> d.getDiaSemana() == diaActual)
                .findFirst()
                .orElse(null);

        if (detalle == null) {
            throw new HorarioAccessException("Acceso denegado: Fuera del horario de trabajo asignado");
        }
        if (!detalle.isActivo()) {
            throw new HorarioAccessException("Acceso denegado: Fuera del horario de trabajo asignado");
        }
        // Rango [horaEntrada, horaSalida] inclusive
        if (horaActual.isBefore(detalle.getHoraEntrada()) || horaActual.isAfter(detalle.getHoraSalida())) {
            throw new HorarioAccessException("Acceso denegado: Fuera del horario de trabajo asignado");
        }
    }

    private DiaSemana mapDayOfWeek(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
