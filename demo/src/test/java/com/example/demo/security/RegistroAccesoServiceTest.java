package com.example.demo.security;

import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroAccesoServiceTest {

    @Mock private UsuarioActualService usuarioActual;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private EpisodioRepository episodioRepository;

    @InjectMocks private RegistroAccesoService service;

    @Test
    void validarEdicion_permiteAlAutorDelRegistro() {
        UsuarioEntity medico = usuario(5L);
        when(usuarioActual.actual()).thenReturn(Optional.of(medico));
        when(usuarioActual.tieneAccesoGlobal(medico)).thenReturn(false);

        assertDoesNotThrow(() -> service.validarEdicion(usuario(5L)));
    }

    @Test
    void validarEdicion_rechazaAOtroUsuario() {
        UsuarioEntity medico = usuario(5L);
        when(usuarioActual.actual()).thenReturn(Optional.of(medico));
        when(usuarioActual.tieneAccesoGlobal(medico)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.validarEdicion(usuario(7L)));
    }

    @Test
    void validarEdicion_permiteAccesoGlobal() {
        UsuarioEntity administrador = usuario(1L);
        when(usuarioActual.actual()).thenReturn(Optional.of(administrador));
        when(usuarioActual.tieneAccesoGlobal(administrador)).thenReturn(true);

        assertDoesNotThrow(() -> service.validarEdicion(usuario(7L)));
    }

    @Test
    void validarAccesoPaciente_rechazaMedicoNoAsignado() {
        when(usuarioActual.medicoRestringidoId()).thenReturn(Optional.of(5L));
        when(pacienteRepository.existsByIdAndMedicos_Id(20L, 5L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.validarAccesoPaciente(20L));
    }

    @Test
    void validarAccesoPaciente_permiteMedicoAsignado() {
        when(usuarioActual.medicoRestringidoId()).thenReturn(Optional.of(5L));
        when(pacienteRepository.existsByIdAndMedicos_Id(20L, 5L)).thenReturn(true);

        assertDoesNotThrow(() -> service.validarAccesoPaciente(20L));
    }

    @Test
    void autorActual_priorizaAlUsuarioAutenticado() {
        UsuarioEntity autenticado = usuario(5L);
        when(usuarioActual.actual()).thenReturn(Optional.of(autenticado));

        assertSame(autenticado, service.autorActual(7L));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void autorActual_sinSesionUsaElUsuarioSolicitado() {
        UsuarioEntity solicitado = usuario(7L);
        when(usuarioActual.actual()).thenReturn(Optional.empty());
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(solicitado));

        assertSame(solicitado, service.autorActual(7L));
    }

    @Test
    void autorParaEdicion_conSesionConservaElAutorOriginal() {
        UsuarioEntity original = usuario(7L);
        when(usuarioActual.actual()).thenReturn(Optional.of(usuario(5L)));

        assertSame(original, service.autorParaEdicion(original, 5L));
    }

    @Test
    void autorParaEdicion_sinSesionDevuelveNullParaCompatibilidad() {
        when(usuarioActual.actual()).thenReturn(Optional.empty());

        assertNull(service.autorParaEdicion(usuario(7L), 5L));
    }

    private UsuarioEntity usuario(Long id) {
        return UsuarioEntity.builder().id(id).username("usuario" + id).build();
    }
}
