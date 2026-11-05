package com.example.demo.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        AuthDTOs.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDTOs.MeResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && StringUtils.hasText(authentication.getName())) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitar-cambio-credenciales")
    public ResponseEntity<AuthDTOs.CambioCredencialesPinResponse> solicitarCambioCredenciales(
            @Valid @RequestBody AuthDTOs.SolicitudCambioCredencialesRequest request) {
        return ResponseEntity.ok(authService.solicitarCambioCredenciales(request));
    }

    @PostMapping("/establecer-credenciales")
    public ResponseEntity<AuthDTOs.AuthResponse> establecerCredenciales(@Valid @RequestBody AuthDTOs.EstablecerCredencialesRequest request) {
        return ResponseEntity.ok(authService.establecerCredenciales(request));
    }
}
