package com.example.demo.modules.usuarios.roles;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermisosSistemaTest {

    @Test
    void administradorTieneTodosLosSubmodulosConTodosLosPermisos() {
        List<PermisosSistema.DefinicionPermiso> permisos = permisosDe("Administrador");

        assertEquals(PermisosSistema.submodulos().size(), permisos.size());
        assertTrue(permisos.stream().allMatch(p -> p.leer() && p.crear() && p.editar() && p.eliminar()));
    }

    @Test
    void directorTieneAccesoCompletoEnModulosAdministrativosYSoloLecturaEnMedicaYEnfermeria() {
        Map<String, PermisosSistema.DefinicionPermiso> permisos = permisosPorCodigo("Director");

        assertModuloCompleto(permisos, "FARMACIA");
        assertModuloCompleto(permisos, "USUARIOS");
        assertModuloCompleto(permisos, "PACIENTES");
        assertModuloCompleto(permisos, "CATALOGOS");
        assertModuloCompleto(permisos, "REPORTES");
        assertModuloSoloLectura(permisos, "MEDICA");
        assertModuloSoloLectura(permisos, "ENFERMERIA");
    }

    @Test
    void medicoSoloTieneGestionMedicaSuCatalogoClinicoYReporteMedico() {
        Map<String, PermisosSistema.DefinicionPermiso> permisos = permisosPorCodigo("Médico");

        assertModuloCompleto(permisos, "MEDICA");
        assertPermisoCompleto(permisos.get("CATALOGOS-CLINICA"));
        assertPermisoSoloLectura(permisos.get("REPORTES-MEDICA"));
        assertEquals(Set.of("MEDICA", "CATALOGOS", "REPORTES"), modulosConAcceso(permisos));
    }

    @Test
    void enfermeraTieneGestionDeEnfermeriaYCatalogosDePacientesYClinica() {
        Map<String, PermisosSistema.DefinicionPermiso> permisos = permisosPorCodigo("Enfermera");

        assertModuloCompleto(permisos, "ENFERMERIA");
        assertPermisoCompleto(permisos.get("CATALOGOS-CLINICA"));
        assertPermisoCompleto(permisos.get("CATALOGOS-PACIENTES"));
        assertPermisoSoloLectura(permisos.get("REPORTES-ENFERMERIA"));
        assertEquals(Set.of("ENFERMERIA", "CATALOGOS", "REPORTES"), modulosConAcceso(permisos));
    }

    @Test
    void farmaceuticoTieneFarmaciaSuCatalogoYReporteDeFarmacia() {
        Map<String, PermisosSistema.DefinicionPermiso> permisos = permisosPorCodigo("Farmaceutico");

        assertModuloCompleto(permisos, "FARMACIA");
        assertPermisoCompleto(permisos.get("CATALOGOS-FARMACIA"));
        assertPermisoSoloLectura(permisos.get("REPORTES-FARMACIA"));
        assertEquals(Set.of("FARMACIA", "CATALOGOS", "REPORTES"), modulosConAcceso(permisos));
    }

    private List<PermisosSistema.DefinicionPermiso> permisosDe(String rol) {
        return PermisosSistema.permisosPorRol().get(rol);
    }

    private Map<String, PermisosSistema.DefinicionPermiso> permisosPorCodigo(String rol) {
        return permisosDe(rol).stream()
                .collect(Collectors.toMap(PermisosSistema.DefinicionPermiso::submoduloCodigo, p -> p));
    }

    private Set<String> modulosConAcceso(Map<String, PermisosSistema.DefinicionPermiso> permisos) {
        return permisos.keySet().stream()
                .map(codigo -> PermisosSistema.submodulos().stream()
                        .filter(s -> s.codigo().equals(codigo))
                        .findFirst()
                        .orElseThrow()
                        .moduloCodigo())
                .collect(Collectors.toSet());
    }

    private void assertModuloCompleto(Map<String, PermisosSistema.DefinicionPermiso> permisos, String modulo) {
        for (String codigo : codigosDeModulo(modulo)) {
            assertPermisoCompleto(permisos.get(codigo));
        }
    }

    private void assertModuloSoloLectura(Map<String, PermisosSistema.DefinicionPermiso> permisos, String modulo) {
        for (String codigo : codigosDeModulo(modulo)) {
            assertPermisoSoloLectura(permisos.get(codigo));
        }
    }

    private Set<String> codigosDeModulo(String modulo) {
        return PermisosSistema.submodulos().stream()
                .filter(s -> s.moduloCodigo().equals(modulo))
                .map(PermisosSistema.DefinicionSubmodulo::codigo)
                .collect(Collectors.toSet());
    }

    private void assertPermisoCompleto(PermisosSistema.DefinicionPermiso permiso) {
        assertTrue(permiso.leer(), "Debe poder leer");
        assertTrue(permiso.crear(), "Debe poder crear");
        assertTrue(permiso.editar(), "Debe poder editar");
        assertTrue(permiso.eliminar(), "Debe poder eliminar");
    }

    private void assertPermisoSoloLectura(PermisosSistema.DefinicionPermiso permiso) {
        assertTrue(permiso.leer(), "Debe poder leer");
        assertFalse(permiso.crear(), "No debe poder crear");
        assertFalse(permiso.editar(), "No debe poder editar");
        assertFalse(permiso.eliminar(), "No debe poder eliminar");
    }
}
