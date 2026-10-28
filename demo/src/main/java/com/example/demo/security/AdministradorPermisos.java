package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AdministradorPermisos implements AuthorizationManager<RequestAuthorizationContext> {

    private final RegistroPermisosApi registroPermisosApi;
    private final PermissionService permissionService;

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {
        Authentication actual = authentication.get();
        if (actual == null || !actual.isAuthenticated() || actual instanceof AnonymousAuthenticationToken) {
            throw new InsufficientAuthenticationException("No autenticado");
        }

        HttpServletRequest request = context.getRequest();
        String metodo = request.getMethod();
        AccionPermiso accion = accion(metodo);
        if (accion == null) {
            return new AuthorizationDecision(false);
        }

        return registroPermisosApi.resolver(request.getRequestURI(), metodo)
                .map(regla -> new AuthorizationDecision(
                        permissionService.puede(actual, regla.modulos(), regla.submodulos(), accion)))
                .orElseGet(() -> new AuthorizationDecision(false));
    }

    private AccionPermiso accion(String metodo) {
        return switch (metodo) {
            case "GET", "HEAD" -> AccionPermiso.LEER;
            case "POST" -> AccionPermiso.CREAR;
            case "PUT", "PATCH" -> AccionPermiso.EDITAR;
            case "DELETE" -> AccionPermiso.ELIMINAR;
            default -> null;
        };
    }
}
