package com.example.demo.security;

import com.example.demo.modules.usuarios.roles.PermisosSistema;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioActualService {

    private static final Set<String> ROLES_ACCESO_GLOBAL = Set.of(
            PermisosSistema.ROL_ADMINISTRADOR,
            PermisosSistema.ROL_DIRECTOR);

    private final UsuarioRepository usuarioRepository;

    /** Usuario autenticado en la petición actual, si existe. */
    public Optional<UsuarioEntity> actual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !StringUtils.hasText(authentication.getName())) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName());
    }

    /**
     * Id del médico autenticado cuando debe ver solo sus pacientes asignados: tiene el rol Médico y
     * no tiene un rol con acceso global (Administrador o Director).
     */
    public Optional<Long> medicoRestringidoId() {
        return actual()
                .filter(usuario -> esMedico(usuario) && !tieneAccesoGlobal(usuario))
                .map(UsuarioEntity::getId);
    }

    private boolean esMedico(UsuarioEntity usuario) {
        return usuario.getRoles().stream()
                .map(RolEntity::getNombre)
                .anyMatch(PermisosSistema.ROL_MEDICO::equals);
    }

    private boolean tieneAccesoGlobal(UsuarioEntity usuario) {
        Set<String> roles = usuario.getRoles().stream()
                .map(RolEntity::getNombre)
                .collect(Collectors.toSet());
        return roles.stream().anyMatch(ROLES_ACCESO_GLOBAL::contains);
    }
}
