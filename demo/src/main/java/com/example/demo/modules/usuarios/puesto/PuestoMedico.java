package com.example.demo.modules.usuarios.puesto;

import java.text.Normalizer;
import java.util.Locale;

/** Regla compartida para identificar puestos de médicos (Médico, Doctor, Doctora). */
public final class PuestoMedico {

    private PuestoMedico() {
    }

    public static boolean esMedico(puestoEntity puesto) {
        if (puesto == null || puesto.getNombre() == null) {
            return false;
        }
        String normalizado = Normalizer.normalize(puesto.getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalizado.contains("medic") || normalizado.contains("doctor");
    }
}
