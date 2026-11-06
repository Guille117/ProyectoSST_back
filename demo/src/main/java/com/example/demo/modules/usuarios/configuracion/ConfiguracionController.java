package com.example.demo.modules.usuarios.configuracion;

import com.example.demo.modules.usuarios.usuarios.UsuarioDTOs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de autoservicio del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService service;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTOs.Response> obtenerPerfil() {
        return ResponseEntity.ok(service.obtenerPerfil());
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioDTOs.Response> actualizarPerfil(
            @Valid @RequestBody UsuarioDTOs.PersonaRequest request) {
        return ResponseEntity.ok(service.actualizarPerfil(request));
    }

    @PutMapping("/usuario")
    public ResponseEntity<ConfiguracionDTOs.CambioUsuarioResponse> cambiarUsuario(
            @Valid @RequestBody ConfiguracionDTOs.CambioUsuarioRequest request) {
        return ResponseEntity.ok(service.cambiarUsuario(request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody ConfiguracionDTOs.CambioPasswordRequest request) {
        service.cambiarPassword(request);
        return ResponseEntity.noContent().build();
    }
}
