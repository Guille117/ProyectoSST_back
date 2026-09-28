package com.example.demo.modules.usuarios.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioRepository;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.puesto.puestoRepository;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioSistemaDataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private puestoRepository puestoRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void run_creaUsuarioSistemaSinDatosPersonalesSensibles() {
        puestoEntity puesto = new puestoEntity();
        puesto.setId(1L);
        puesto.setNombre("Administrador");
        HorarioEntity horario = HorarioEntity.builder().id(2L).nombre("Sin limite").build();
        RolEntity rol = RolEntity.builder().id(3L).nombre("Administrador").build();

        when(puestoRepository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(puesto));
        when(horarioRepository.findByNombreIgnoreCase("Sin limite")).thenReturn(Optional.of(horario));
        when(rolRepository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(rol));
        when(usuarioRepository.findByUsernameIgnoreCase("guille117")).thenReturn(Optional.empty());
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.existsByCodigo(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new UsuarioSistemaDataInitializer(usuarioRepository, puestoRepository, horarioRepository,
                rolRepository, passwordEncoder).run();

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        UsuarioEntity sistema = captor.getValue();
        PersonaEntity persona = sistema.getPersona();

        assertEquals("guille117", sistema.getUsername());
        assertEquals("encoded-password", sistema.getPassword());
        assertTrue(sistema.isEstado());
        assertEquals(puesto, sistema.getPuesto());
        assertEquals(horario, sistema.getHorario());
        assertEquals(Set.of(rol), sistema.getRoles());
        assertEquals("Sistema", persona.getNombres());
        assertEquals("Sistema", persona.getApellidos());
        assertNull(persona.getCui());
        assertNull(persona.getSexo());
        assertNull(persona.getFechaNacimiento());
        assertNull(persona.getTelefono());
        assertNull(persona.getEmail());
    }
}