package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioRepository;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.puesto.puestoRepository;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(4)
public class UsuarioSistemaDataInitializer implements CommandLineRunner {

    private static final String USERNAME = "guille117";
    private static final String PASSWORD = "2210@Skipper";

    private final UsuarioRepository usuarioRepository;
    private final puestoRepository puestoRepository;
    private final HorarioRepository horarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        puestoEntity puesto = puestoRepository.findByNombreIgnoreCase("Administrador")
                .orElseThrow(() -> new IllegalStateException("No existe el puesto del sistema Administrador"));
        HorarioEntity horario = horarioRepository.findByNombreIgnoreCase("Sin limite")
                .orElseThrow(() -> new IllegalStateException("No existe el horario del sistema Sin limite"));
        RolEntity rol = rolRepository.findByNombreIgnoreCase("Administrador")
                .orElseThrow(() -> new IllegalStateException("No existe el rol del sistema Administrador"));

        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(USERNAME)
                .orElseGet(UsuarioEntity::new);
        PersonaEntity persona = usuario.getPersona() != null ? usuario.getPersona() : new PersonaEntity();
        persona.setCui(null);
        persona.setSexo(null);
        persona.setFechaNacimiento(null);
        persona.setTelefono(null);
        persona.setEmail(null);
        persona.asignarNombres("Sistema", "Sistema");

        if (usuario.getCodigo() == null) {
            usuario.setCodigo(generarCodigoUsuario());
        }
        usuario.setPersona(persona);
        usuario.setPuesto(puesto);
        usuario.setHorario(horario);
        usuario.setRoles(new HashSet<>(Set.of(rol)));
        usuario.setUsername(USERNAME);
        usuario.setEspecialidad(null);
        usuario.setPassword(passwordEncoder.encode(PASSWORD));
        usuario.setEstado(true);
        usuarioRepository.save(usuario);
    }

    private String generarCodigoUsuario() {
        long siguiente = usuarioRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("USR-%02d", siguiente++);
        } while (usuarioRepository.existsByCodigo(codigo));
        return codigo;
    }
}