package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioRepository;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.puesto.puestoRepository;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@Order(4)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class UsuarioSistemaDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final puestoRepository puestoRepository;
    private final HorarioRepository horarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public UsuarioSistemaDataInitializer(UsuarioRepository usuarioRepository,
            puestoRepository puestoRepository,
            HorarioRepository horarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.puestoRepository = puestoRepository;
        this.horarioRepository = horarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            log.info("Creación del administrador omitida: APP_ADMIN_USERNAME/APP_ADMIN_PASSWORD no configurados");
            return;
        }

        String username = adminUsername.trim();
        Optional<UsuarioEntity> existente = usuarioRepository.findByUsernameIgnoreCase(username);
        if (existente.isPresent()) {
            log.info("El usuario administrador '{}' ya existe; no se modifica", username);
            return;
        }

        puestoEntity puesto = puestoRepository.findByNombreIgnoreCase("Administrador")
                .orElseThrow(() -> new IllegalStateException("No existe el puesto del sistema Administrador"));
        HorarioEntity horario = horarioRepository.findByNombreIgnoreCase("Sin limite")
                .or(() -> horarioRepository.findByNombreIgnoreCase("Horario Libre"))
                .orElseThrow(() -> new IllegalStateException("No existe el horario del sistema Sin limite"));
        RolEntity rol = rolRepository.findByNombreIgnoreCase("Administrador")
                .orElseThrow(() -> new IllegalStateException("No existe el rol del sistema Administrador"));

        UsuarioEntity usuario = new UsuarioEntity();
        PersonaEntity persona = new PersonaEntity();
        persona.setCui(null);
        persona.setSexo(null);
        persona.setFechaNacimiento(null);
        persona.setTelefono(null);
        persona.setEmail(null);
        persona.asignarNombres("Sistema", "Sistema");

        usuario.setCodigo(generarCodigoUsuario());
        usuario.setPersona(persona);
        usuario.setPuesto(puesto);
        usuario.setHorario(horario);
        usuario.setRoles(new HashSet<>(Set.of(rol)));
        usuario.setUsername(username);
        usuario.setEspecialidad(null);
        usuario.setPassword(passwordEncoder.encode(adminPassword));
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
