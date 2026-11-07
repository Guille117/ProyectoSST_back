package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.PuestoMedico;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Construye el nombre corto de un usuario para los pies de las tarjetas: primer nombre,
 * primer apellido y, cuando el usuario es médico, el prefijo Dr. o Dra. según su sexo.
 */
public final class UsuarioNombreCorto {

    private UsuarioNombreCorto() {
    }

    public static String formatear(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        PersonaEntity persona = usuario.getPersona();
        String primerNombre = limpiar(persona != null ? persona.getPrimerNombre() : null);
        String primerApellido = limpiar(persona != null ? persona.getPrimerApellido() : null);

        String nombre = Stream.of(primerNombre, primerApellido)
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(Collectors.joining(" "));

        if (nombre.isBlank() && persona != null) {
            nombre = limpiar(persona.getNombreCompleto());
        }
        if (nombre.isBlank()) {
            nombre = limpiar(usuario.getUsername());
        }
        if (nombre.isBlank()) {
            return null;
        }

        if (PuestoMedico.esMedico(usuario.getPuesto())) {
            boolean femenino = persona != null && persona.getSexo() == Sexo.FEMENINO;
            return (femenino ? "Dra. " : "Dr. ") + nombre;
        }
        return nombre;
    }

    private static String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
