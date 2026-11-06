package com.example.demo.security;

import com.example.demo.modules.usuarios.horarios.DiaSemana;
import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioSemanalDetalleEntity;
import com.example.demo.modules.usuarios.horarios.HorarioTurnoDetalleEntity;
import com.example.demo.modules.usuarios.puesto.PuestoMedico;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class HorarioLaboralService {

    private static final String FUERA_DE_HORARIO =
            "Acceso denegado: fuera del horario de trabajo asignado";
    private static final String FUERA_DE_GUARDIA =
            "Acceso denegado: fuera del período de guardia";

    private final UsuarioActualService usuarioActual;
    private final UsuarioRepository usuarioRepository;

    /**
     * Valida el horario de un usuario cargado dentro de una transacción para que
     * sus detalles perezosos (días de la semana) estén disponibles.
     */
    @Transactional(readOnly = true)
    public void validarAccesoPorUsername(String username) {
        usuarioRepository.findByUsernameIgnoreCase(username)
                .ifPresent(usuario -> validarAcceso(usuario, LocalDateTime.now()));
    }

    public void validarAcceso(UsuarioEntity usuario, LocalDateTime ahora) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        // El administrador y los médicos no están sujetos a horario.
        if (usuarioActual.esAdministrador(usuario) || PuestoMedico.esMedico(usuario.getPuesto())) {
            return;
        }

        HorarioEntity horario = usuario.getHorario();
        if (horario == null) {
            throw new HorarioAccessException("Acceso denegado: no tiene un horario asignado");
        }

        LocalDateTime momento = ahora != null ? ahora : LocalDateTime.now();
        if (horario.isEsRotativo()) {
            validarGuardia(usuario, horario, momento);
        } else {
            validarSemanal(horario, momento);
        }
    }

    private void validarSemanal(HorarioEntity horario, LocalDateTime momento) {
        DiaSemana diaActual = mapDayOfWeek(momento.getDayOfWeek());
        HorarioSemanalDetalleEntity detalle = horario.getSemanalDetalles() == null
                ? null
                : horario.getSemanalDetalles().stream()
                        .filter(d -> d.getDiaSemana() == diaActual)
                        .findFirst()
                        .orElse(null);

        if (detalle == null || !detalle.isActivo()) {
            throw new HorarioAccessException(FUERA_DE_HORARIO);
        }

        LocalTime horaActual = momento.toLocalTime();
        if (horaActual.isBefore(detalle.getHoraEntrada()) || horaActual.isAfter(detalle.getHoraSalida())) {
            throw new HorarioAccessException(FUERA_DE_HORARIO);
        }
    }

    private void validarGuardia(UsuarioEntity usuario, HorarioEntity horario, LocalDateTime momento) {
        LocalDateTime inicio = usuario.getInicioGuardia();
        if (inicio == null) {
            throw new HorarioAccessException("Acceso denegado: no tiene registrado el inicio de guardia");
        }

        HorarioTurnoDetalleEntity turno = horario.getTurnoDetalle();
        if (turno == null || turno.getHorasTrabajo() == null || turno.getHorasTrabajo() <= 0) {
            throw new HorarioAccessException("Acceso denegado: el horario de guardia no está configurado");
        }

        long minutosTrabajo = turno.getHorasTrabajo() * 60L;
        long minutosDescanso = turno.getHorasDescanso() == null ? 0L : turno.getHorasDescanso() * 60L;
        long ciclo = minutosTrabajo + minutosDescanso;

        long transcurrido = Duration.between(inicio, momento).toMinutes();
        if (transcurrido < 0) {
            throw new HorarioAccessException(FUERA_DE_GUARDIA);
        }

        long posicion = transcurrido % ciclo;
        if (posicion >= minutosTrabajo) {
            throw new HorarioAccessException(FUERA_DE_GUARDIA);
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
