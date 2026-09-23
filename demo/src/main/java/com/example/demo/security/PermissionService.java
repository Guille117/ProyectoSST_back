package com.example.demo.security;

import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, String submoduloCodigo) {
        return tienePermiso(authentication, submoduloCodigo, Permiso.CREATE);
    }

    @Transactional(readOnly = true)
    public boolean canEdit(Authentication authentication, String submoduloCodigo) {
        return tienePermiso(authentication, submoduloCodigo, Permiso.EDIT);
    }

    @Transactional(readOnly = true)
    public boolean canDelete(Authentication authentication, String submoduloCodigo) {
        return tienePermiso(authentication, submoduloCodigo, Permiso.DELETE);
    }

    @Transactional(readOnly = true)
    public boolean canCreateCatalog(Authentication authentication) {
        return canCreate(authentication, "CATALOGOS");
    }

    @Transactional(readOnly = true)
    public boolean canEditCatalog(Authentication authentication) {
        return canEdit(authentication, "CATALOGOS");
    }

    @Transactional(readOnly = true)
    public boolean canCreateCurrentRequest(Authentication authentication) {
        return canCreate(authentication, obtenerCodigoSubmoduloActual());
    }

    @Transactional(readOnly = true)
    public boolean canEditCurrentRequest(Authentication authentication) {
        return canEdit(authentication, obtenerCodigoSubmoduloActual());
    }

    private String obtenerCodigoSubmoduloActual() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getRequest() == null) {
            return "";
        }
        String[] partes = attributes.getRequest().getRequestURI().split("/");
        return partes.length > 3 ? partes[3].toUpperCase(java.util.Locale.ROOT) : "";
    }

    private boolean tienePermiso(Authentication authentication, String submoduloCodigo, Permiso permiso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(usuario -> usuario.getRoles().stream()
                        .flatMap(rol -> rol.getPermisos().stream())
                        .filter(rolPermiso -> rolPermiso.getSubmodulo() != null
                                && submoduloCodigo.equalsIgnoreCase(rolPermiso.getSubmodulo().getCodigo()))
                        .anyMatch(rolPermiso -> switch (permiso) {
                            case CREATE -> rolPermiso.isPuedeCrear();
                            case EDIT -> rolPermiso.isPuedeEditar();
                            case DELETE -> rolPermiso.isPuedeEliminar();
                        }))
                .orElse(false);
    }

    private enum Permiso {
        CREATE, EDIT, DELETE
    }
}