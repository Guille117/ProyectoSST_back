package com.example.demo.modules.clinica.indicaciones;

public final class DuracionValidator {

    private DuracionValidator() {
    }

    public static Integer validar(TipoDuracion tipoDuracion, Integer duracion) {
        if (tipoDuracion == null) {
            throw new IllegalArgumentException("El tipo de duración es obligatorio");
        }

        if (tipoDuracion == TipoDuracion.DIAS) {
            if (duracion == null) {
                throw new IllegalArgumentException("La duración es obligatoria cuando el tipo es DIAS");
            }
            if (duracion <= 0) {
                throw new IllegalArgumentException("La duración debe ser mayor a 0");
            }
            return duracion;
        }

        return null;
    }
}
