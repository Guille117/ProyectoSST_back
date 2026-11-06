package com.example.demo.security;

import com.example.demo.modules.usuarios.horarios.DiaSemana;
import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioSemanalDetalleEntity;
import com.example.demo.modules.usuarios.horarios.HorarioTurnoDetalleEntity;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.roles.PermisosSistema;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioLaboralServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private HorarioLaboralService service() {
        return new HorarioLaboralService(new UsuarioActualService(usuarioRepository), usuarioRepository);
    }

    @Test
    void validarAccesoPorUsername_buscaElUsuarioYDeniegaFueraDeHorario() {
        when(usuarioRepository.findByUsernameIgnoreCase("enfermera"))
                .thenReturn(java.util.Optional.of(usuarioNoMedico(null)));

        assertThrows(HorarioAccessException.class,
                () -> service().validarAccesoPorUsername("enfermera"));
    }

    @Test
    void usuarioSemanalDentroDeHorarioPermiteAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioSemanal());

        assertDoesNotThrow(() -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 24, 10, 0)));
    }

    @Test
    void usuarioSemanalFueraDeHorarioDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioSemanal());

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 24, 20, 0)));
    }

    @Test
    void usuarioSemanalEnDiaNoConfiguradoDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioSemanal());

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 25, 10, 0)));
    }

    @Test
    void medicoSinHorarioPermiteAcceso() {
        UsuarioEntity medico = UsuarioEntity.builder()
                .id(1L)
                .puesto(puesto("Médico"))
                .horario(null)
                .roles(Set.of(rol(PermisosSistema.ROL_MEDICO)))
                .build();

        assertDoesNotThrow(() -> service().validarAcceso(medico, LocalDateTime.now()));
    }

    @Test
    void administradorFueraDeHorarioPermiteAcceso() {
        UsuarioEntity admin = usuarioNoMedico(horarioSemanal());
        admin.setRoles(Set.of(rol(PermisosSistema.ROL_ADMINISTRADOR)));

        assertDoesNotThrow(() -> service().validarAcceso(admin,
                LocalDateTime.of(2026, 8, 24, 23, 0)));
    }

    @Test
    void directorDebeCumplirHorario() {
        UsuarioEntity director = usuarioNoMedico(horarioSemanal());
        director.setRoles(Set.of(rol(PermisosSistema.ROL_DIRECTOR)));

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(director,
                LocalDateTime.of(2026, 8, 24, 23, 0)));
    }

    @Test
    void usuarioNoMedicoSinHorarioDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(null);

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 24, 10, 0)));
    }

    @Test
    void guardiaAntesDeInicioDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioGuardia());
        usuario.setInicioGuardia(LocalDateTime.of(2026, 8, 24, 8, 0));

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 24, 7, 0)));
    }

    @Test
    void guardiaDentroDeHorasDeTrabajoPermiteAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioGuardia());
        usuario.setInicioGuardia(LocalDateTime.of(2026, 8, 24, 8, 0));

        assertDoesNotThrow(() -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 25, 7, 59)));
    }

    @Test
    void guardiaDuranteDescansoDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioGuardia());
        usuario.setInicioGuardia(LocalDateTime.of(2026, 8, 24, 8, 0));

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 25, 8, 0)));
    }

    @Test
    void guardiaReiniciaCicloTrasTrabajoYDescanso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioGuardia());
        usuario.setInicioGuardia(LocalDateTime.of(2026, 8, 24, 8, 0));

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 27, 7, 59)));
        assertDoesNotThrow(() -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 27, 8, 0)));
    }

    @Test
    void guardiaSinInicioRegistradoDeniegaAcceso() {
        UsuarioEntity usuario = usuarioNoMedico(horarioGuardia());

        assertThrows(HorarioAccessException.class, () -> service().validarAcceso(usuario,
                LocalDateTime.of(2026, 8, 24, 10, 0)));
    }

    private UsuarioEntity usuarioNoMedico(HorarioEntity horario) {
        return UsuarioEntity.builder()
                .id(1L)
                .puesto(puesto("Enfermera"))
                .horario(horario)
                .roles(Set.of(rol(PermisosSistema.ROL_ENFERMERA)))
                .build();
    }

    private puestoEntity puesto(String nombre) {
        puestoEntity puesto = new puestoEntity();
        puesto.setNombre(nombre);
        puesto.setEstado(true);
        return puesto;
    }

    private RolEntity rol(String nombre) {
        return RolEntity.builder().nombre(nombre).build();
    }

    private HorarioEntity horarioSemanal() {
        HorarioEntity horario = HorarioEntity.builder()
                .id(1L)
                .codigo("HOR-01")
                .nombre("Diurno")
                .esRotativo(false)
                .estado(true)
                .build();
        horario.setSemanalDetalles(List.of(HorarioSemanalDetalleEntity.builder()
                .id(10L)
                .horario(horario)
                .diaSemana(DiaSemana.LUNES)
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(17, 0))
                .activo(true)
                .build()));
        return horario;
    }

    private HorarioEntity horarioGuardia() {
        HorarioEntity horario = HorarioEntity.builder()
                .id(2L)
                .codigo("HOR-02")
                .nombre("Guardia médica")
                .esRotativo(true)
                .estado(true)
                .build();
        horario.setTurnoDetalle(HorarioTurnoDetalleEntity.builder()
                .id(20L)
                .horario(horario)
                .horasTrabajo(24)
                .horasDescanso(48)
                .build());
        return horario;
    }
}
