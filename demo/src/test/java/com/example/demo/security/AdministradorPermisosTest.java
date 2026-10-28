package com.example.demo.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministradorPermisosTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private Authentication authentication;

    @Test
    void autorizaCuandoLaReglaYElPermisoCoinciden() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(permissionService.puede(any(), anyCollection(), anyCollection(), eq(AccionPermiso.LEER)))
                .thenReturn(true);
        AdministradorPermisos manager = new AdministradorPermisos(new RegistroPermisosApi(), permissionService);

        AuthorizationResult decision = manager.authorize(() -> authentication, contexto("/api/v1/usuarios", "GET"));

        assertTrue(decision.isGranted());
    }

    @Test
    void deniegaRutaDesconocida() {
        when(authentication.isAuthenticated()).thenReturn(true);
        AdministradorPermisos manager = new AdministradorPermisos(new RegistroPermisosApi(), permissionService);

        AuthorizationResult decision = manager.authorize(() -> authentication, contexto("/api/v1/desconocida", "GET"));

        assertFalse(decision.isGranted());
    }

    @Test
    void lanzaNoAutenticadoCuandoNoHaySesionReal() {
        AnonymousAuthenticationToken anonimo = new AnonymousAuthenticationToken(
                "llave", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        AdministradorPermisos manager = new AdministradorPermisos(new RegistroPermisosApi(), permissionService);

        assertThrows(InsufficientAuthenticationException.class,
                () -> manager.authorize(() -> anonimo, contexto("/api/v1/usuarios", "GET")));
    }

    private RequestAuthorizationContext contexto(String ruta, String metodo) {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, ruta);
        return new RequestAuthorizationContext(request);
    }
}
