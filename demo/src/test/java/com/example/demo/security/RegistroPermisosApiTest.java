package com.example.demo.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroPermisosApiTest {

    private final RegistroPermisosApi registro = new RegistroPermisosApi();

    @Test
    void usuariosMedicosSeResuelveAntesQueUsuariosGenerales() {
        ReglaPermiso regla = registro.resolver("/api/v1/usuarios/medicos", "GET").orElseThrow();

        assertTrue(regla.modulos().contains("PACIENTES"));
        assertTrue(regla.modulos().contains("MEDICA"));
    }

    @Test
    void enfermeraPuedeCrearProcedimientoPeroNoEliminarlo() {
        ReglaPermiso creacion = registro.resolver("/api/v1/procedimientosMedicos", "POST").orElseThrow();
        ReglaPermiso eliminacion = registro.resolver("/api/v1/procedimientosMedicos/5", "DELETE").orElseThrow();

        assertTrue(creacion.modulos().contains("ENFERMERIA"));
        assertFalse(eliminacion.modulos().contains("ENFERMERIA"));
    }

    @Test
    void medicoPuedeLeerDatosDeEnfermeria() {
        ReglaPermiso regla = registro.resolver("/api/v1/enfermeria/signos-vitales", "GET").orElseThrow();

        assertTrue(regla.modulos().contains("MEDICA"));
        assertTrue(regla.modulos().contains("ENFERMERIA"));
    }

    @Test
    void rutaDesconocidaNoTieneRegla() {
        assertTrue(registro.resolver("/api/v1/ruta-inexistente", "GET").isEmpty());
    }
}
