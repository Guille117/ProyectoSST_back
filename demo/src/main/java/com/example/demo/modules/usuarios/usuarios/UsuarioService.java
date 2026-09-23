package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.horarios.HorarioRepository;
import com.example.demo.modules.usuarios.puesto.puestoEntity;
import com.example.demo.modules.usuarios.puesto.puestoRepository;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PersonaRepository personaRepository;
    private final puestoRepository puestoRepository;
    private final HorarioRepository horarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioPinRepository pinRepository;

    @Transactional
    public String crear(UsuarioDTOs.Request req) {
        if (req == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        String cui = StringNormalizer.normalizarTexto(req.persona().cui());
        String nombres = StringNormalizer.normalizarTexto(req.persona().nombres());
        String apellidos = StringNormalizer.normalizarTexto(req.persona().apellidos());
        String username = StringNormalizer.normalizarTexto(req.username());
        String telefono = StringNormalizer.normalizarNullable(req.persona().telefono());
        String email = StringNormalizer.normalizarNullable(req.persona().email());
        NombrePersonaParser.PartesNombre partesNombre = NombrePersonaParser.separar(nombres, apellidos);

        validarNoDuplicado(null, cui, username);

        // Validar existen referencias
        puestoEntity puesto = puestoRepository.findById(req.puestoId())
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado con el ID: " + req.puestoId()));
        var horario = horarioRepository.findById(req.horarioId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con el ID: " + req.horarioId()));
        Set<RolEntity> roles = cargarRoles(req.rolIds());

        PersonaEntity persona = PersonaEntity.builder()
                .cui(cui)
                .nombres(nombres)
                .apellidos(apellidos)
                .primerNombre(partesNombre.primerNombre())
                .segundoNombre(partesNombre.segundoNombre())
                .otrosNombres(partesNombre.otrosNombres())
                .primerApellido(partesNombre.primerApellido())
                .segundoApellido(partesNombre.segundoApellido())
                .sexo(req.persona().sexo())
                .fechaNacimiento(req.persona().fechaNacimiento())
                .telefono(telefono)
                .email(email)
                .build();

        // Sin password: se define luego mediante el PIN de un solo uso (ver establecerCredenciales).
        UsuarioEntity entity = UsuarioEntity.builder()
                .codigo(generarCodigoUsuario())
                .persona(persona)
                .puesto(puesto)
                .horario(horario)
                .roles(roles)
                .username(username)
                .estado(req.estado() != null ? req.estado() : true)
                .build();

        UsuarioEntity guardado = repository.save(entity);
        UsuarioPinEntity pin = generarPin(guardado);

        return pin.getCodigo();
    }

    private UsuarioPinEntity generarPin(UsuarioEntity usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        UsuarioPinEntity pin = UsuarioPinEntity.builder()
                .usuario(usuario)
                .codigo(generarCodigoPin())
                .fechaCreacion(ahora)
                .fechaExpiracion(ahora.plusHours(1))
                .usado(false)
                .intentos(0)
                .build();
        return pinRepository.save(pin);
    }

    private String generarCodigoPin() {
        // SecureRandom para que el PIN no sea predecible (es la única credencial en el primer ingreso).
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    @Transactional
    public UsuarioDTOs.Response actualizar(Long id, UsuarioDTOs.UpdateRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        UsuarioEntity existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String cui = StringNormalizer.normalizarTexto(req.persona().cui());
        String nombres = StringNormalizer.normalizarTexto(req.persona().nombres());
        String apellidos = StringNormalizer.normalizarTexto(req.persona().apellidos());
        String username = StringNormalizer.normalizarTexto(req.username());
        String telefono = StringNormalizer.normalizarNullable(req.persona().telefono());
        String email = StringNormalizer.normalizarNullable(req.persona().email());
        NombrePersonaParser.PartesNombre partesNombre = NombrePersonaParser.separar(nombres, apellidos);

        validarNoDuplicado(id, cui, username);

        // validar password si viene con valor
        if (req.password() != null && !req.password().isBlank()) {
            if (req.confirmPassword() == null || !req.password().equals(req.confirmPassword())) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            existente.setPassword(passwordEncoder.encode(req.password()));
        }

        puestoEntity puesto = puestoRepository.findById(req.puestoId())
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado con el ID: " + req.puestoId()));
        var horario = horarioRepository.findById(req.horarioId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con el ID: " + req.horarioId()));
        Set<RolEntity> roles = cargarRoles(req.rolIds());

        PersonaEntity persona = existente.getPersona();
        persona.setCui(cui);
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        persona.setPrimerNombre(partesNombre.primerNombre());
        persona.setSegundoNombre(partesNombre.segundoNombre());
        persona.setOtrosNombres(partesNombre.otrosNombres());
        persona.setPrimerApellido(partesNombre.primerApellido());
        persona.setSegundoApellido(partesNombre.segundoApellido());
        persona.setSexo(req.persona().sexo());
        persona.setFechaNacimiento(req.persona().fechaNacimiento());
        persona.setTelefono(telefono);
        persona.setEmail(email);

        existente.setPuesto(puesto);
        existente.setHorario(horario);
        existente.setRoles(roles);
        existente.setUsername(username);
        existente.setEstado(req.estado() != null ? req.estado() : existente.isEstado());

        UsuarioEntity actualizado = repository.save(existente);
        return mapper.toDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public UsuarioDTOs.Response obtenerPorId(Long id) {
        UsuarioEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTOs.ListResponse> obtenerTodos(Boolean activos) {
        if (activos == null) {
            return repository.findAll().stream()
                    .map(mapper::toListDTO)
                    .toList();
        }
        return repository.findByEstado(activos).stream()
                .map(mapper::toListDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTOs.Response> buscarPorCriterio(String criterio, Boolean activos) {
        if (criterio == null || criterio.isBlank()) {
            return List.of();
        }
        String c = criterio.trim().toLowerCase();

        List<UsuarioEntity> base;
        if (activos == null) {
            base = repository.findAll();
        } else {
            base = repository.findByEstado(activos);
        }

        return base.stream()
                .filter(u -> {
                    PersonaEntity p = u.getPersona();
                    return (p != null && (
                            p.getCui() != null && p.getCui().toLowerCase().contains(c) ||
                            p.getNombres() != null && p.getNombres().toLowerCase().contains(c) ||
                            p.getApellidos() != null && p.getApellidos().toLowerCase().contains(c)
                    )) ||
                    (u.getUsername() != null && u.getUsername().toLowerCase().contains(c)) ||
                    (u.getCodigo() != null && u.getCodigo().toLowerCase().contains(c));
                })
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTOs.Response> buscarPorNombre(String nombre, Boolean activos) {
        return buscarPorCriterio(nombre, activos);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        UsuarioEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));
        entity.setEstado(!entity.isEstado());
        repository.save(entity);
    }

    private void validarNoDuplicado(Long idActual, String cui, String username) {
        // CUI único
        Optional<PersonaEntity> personaDup = personaRepository.findByCui(cui);
        if (personaDup.isPresent()) {
            // buscar usuario que posee esa persona
            Optional<UsuarioEntity> usuarioConCui = repository.findAll().stream()
                    .filter(u -> u.getPersona() != null && u.getPersona().getCui().equals(cui))
                    .findFirst();
            if (usuarioConCui.isPresent() && !usuarioConCui.get().getId().equals(idActual)) {
                if (usuarioConCui.get().isEstado()) {
                    throw new IllegalArgumentException("Ya existe un usuario activo con el CUI: " + cui);
                }
                throw new IllegalArgumentException("Ya existe un usuario con el CUI: " + cui + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo.");
            }
        }

        // Username único
        Optional<UsuarioEntity> dupUser = repository.findByUsernameIgnoreCase(username);
        if (dupUser.isEmpty() || dupUser.get().getId().equals(idActual)) {
            return;
        }
        if (dupUser.get().isEstado()) {
            throw new IllegalArgumentException("Ya existe un usuario activo con el username: " + username);
        }
        throw new IllegalArgumentException("Ya existe un usuario con el username: " + username + " pero está inactivo. Actívalo primero o actualiza ese registro inactivo.");
    }

    private Set<RolEntity> cargarRoles(List<Long> rolIds) {
        if (rolIds == null || rolIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un rol");
        }
        Set<Long> idsUnicos = new HashSet<>(rolIds);
        if (idsUnicos.size() != rolIds.size()) {
            throw new IllegalArgumentException("No se puede repetir un rol");
        }
        List<RolEntity> roles = rolRepository.findAllById(rolIds);
        if (roles.size() != idsUnicos.size()) {
            throw new RuntimeException("Uno o más roles no existen");
        }
        return new HashSet<>(roles);
    }

    private String generarCodigoUsuario() {
        long total = repository.count();
        return String.format("USR-%02d", total + 1);
    }
}
