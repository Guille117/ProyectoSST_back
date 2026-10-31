package com.example.demo.security;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void enfermeraYMedicoPuedenSubirArchivosDeExamen() {
        ReglaPermiso regla = registro.resolver("/api/v1/examenes/5/archivos", "POST").orElseThrow();

        assertTrue(regla.modulos().contains("ENFERMERIA"));
        assertTrue(regla.modulos().contains("MEDICA"));
    }

    @Test
    void enfermeraNoPuedeCrearNiEditarExamenes() {
        ReglaPermiso creacion = registro.resolver("/api/v1/examenes", "POST").orElseThrow();
        ReglaPermiso edicion = registro.resolver("/api/v1/examenes/5", "PUT").orElseThrow();

        assertFalse(creacion.modulos().contains("ENFERMERIA"));
        assertFalse(edicion.modulos().contains("ENFERMERIA"));
    }

    @Test
    void enfermeraPuedeLeerDocumentacionMedicaPeroNoModificarla() {
        List<String> rutas = List.of(
                "/api/v1/historiaClinica/buscar",
                "/api/v1/examenFisico/buscar",
                "/api/v1/diagnostico/buscar",
                "/api/v1/evolucion/buscar",
                "/api/v1/altas/buscar");

        for (String ruta : rutas) {
            ReglaPermiso lectura = registro.resolver(ruta, "GET").orElseThrow();
            assertTrue(lectura.modulos().contains("ENFERMERIA"), ruta);

            for (String metodo : List.of("POST", "PUT", "DELETE")) {
                ReglaPermiso escritura = registro.resolver(ruta, metodo).orElseThrow();
                assertFalse(escritura.modulos().contains("ENFERMERIA"),
                        ruta + " " + metodo);
            }
        }
    }
}
