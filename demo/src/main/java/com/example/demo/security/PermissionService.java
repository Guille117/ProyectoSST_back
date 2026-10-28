package com.example.demo.security;

import com.example.demo.modules.usuarios.roles.RolPermisoEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public boolean canRead(Authentication authentication, String submoduloCodigo) {
        return puedeSubmodulo(authentication, submoduloCodigo, AccionPermiso.LEER);
    }

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, String submoduloCodigo) {
        return puedeSubmodulo(authentication, submoduloCodigo, AccionPermiso.CREAR);
    }

    @Transactional(readOnly = true)
    public boolean canEdit(Authentication authentication, String submoduloCodigo) {
        return puedeSubmodulo(authentication, submoduloCodigo, AccionPermiso.EDITAR);
    }

    @Transactional(readOnly = true)
    public boolean canDelete(Authentication authentication, String submoduloCodigo) {
        return puedeSubmodulo(authentication, submoduloCodigo, AccionPermiso.ELIMINAR);
    }

    @Transactional(readOnly = true)
    public boolean puedeModulo(Authentication authentication, String moduloCodigo, AccionPermiso accion) {
        return puede(authentication, Set.of(moduloCodigo), Set.of(), accion);
    }

    @Transactional(readOnly = true)
    public boolean puedeSubmodulo(Authentication authentication, String submoduloCodigo, AccionPermiso accion) {
        return puede(authentication, Set.of(), Set.of(submoduloCodigo), accion);
    }

    @Transactional(readOnly = true)
    public boolean puede(Authentication authentication, Collection<String> modulos, Collection<String> submodulos,
            AccionPermiso accion) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }
        if ((modulos == null || modulos.isEmpty()) && (submodulos == null || submodulos.isEmpty())) {
            return false;
        }

        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(usuario -> usuario.getRoles().stream()
                        .flatMap(rol -> rol.getPermisos().stream())
                        .anyMatch(permiso -> coincide(permiso, modulos, submodulos)
                                && permite(permiso, accion)))
                .orElse(false);
    }

    private boolean coincide(RolPermisoEntity permiso, Collection<String> modulos,
            Collection<String> submodulos) {
        if (permiso.getSubmodulo() == null) {
            return false;
        }
        boolean coincideModulo = modulos != null && permiso.getSubmodulo().getModulo() != null
                && modulos.contains(permiso.getSubmodulo().getModulo().getCodigo());
        boolean coincideSubmodulo = submodulos != null
                && submodulos.contains(permiso.getSubmodulo().getCodigo());
        return coincideModulo || coincideSubmodulo;
    }

    private boolean permite(RolPermisoEntity permiso, AccionPermiso accion) {
        return switch (accion) {
            case LEER -> permiso.isPuedeLeer();
            case CREAR -> permiso.isPuedeCrear();
            case EDITAR -> permiso.isPuedeEditar();
            case ELIMINAR -> permiso.isPuedeEliminar();
        };
    }
}
