package com.example.demo.modules.usuarios.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PermisosSistema {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_DIRECTOR = "Director";
    public static final String ROL_MEDICO = "Médico";
    public static final String ROL_ENFERMERA = "Enfermera";
    public static final String ROL_FARMACEUTICO = "Farmaceutico";

    public static final String MODULO_FARMACIA = "FARMACIA";
    public static final String MODULO_USUARIOS = "USUARIOS";
    public static final String MODULO_PACIENTES = "PACIENTES";
    public static final String MODULO_MEDICA = "MEDICA";
    public static final String MODULO_ENFERMERIA = "ENFERMERIA";
    public static final String MODULO_CATALOGOS = "CATALOGOS";
    public static final String MODULO_REPORTES = "REPORTES";

    private static final List<DefinicionSubmodulo> SUBMODULOS = List.of(
            submodulo(MODULO_FARMACIA, "Farmacia", "PROVEEDORES", "Proveedores"),
            submodulo(MODULO_FARMACIA, "Farmacia", "COMPRAS", "Compras"),
            submodulo(MODULO_FARMACIA, "Farmacia", "SALIDAS", "Salidas"),
            submodulo(MODULO_FARMACIA, "Farmacia", "DEVOLUCIONES", "Devoluciones"),
            submodulo(MODULO_FARMACIA, "Farmacia", "UNIDADMEDIDA", "Unidades de medida"),
            submodulo(MODULO_FARMACIA, "Farmacia", "INVENTARIO", "Inventario"),
            submodulo(MODULO_FARMACIA, "Farmacia", "MEDICAMENTOS", "Medicamentos"),
            submodulo(MODULO_FARMACIA, "Farmacia", "INSUMOS", "Insumos"),

            submodulo(MODULO_USUARIOS, "Usuarios", "USUARIOS", "Usuarios"),
            submodulo(MODULO_USUARIOS, "Usuarios", "ROLES", "Roles"),
            submodulo(MODULO_USUARIOS, "Usuarios", "HORARIOS", "Horarios"),

            submodulo(MODULO_PACIENTES, "Pacientes", "PACIENTES", "Pacientes"),
            submodulo(MODULO_PACIENTES, "Pacientes", "TIPOS-CAMA", "Tipos de cama"),
            submodulo(MODULO_PACIENTES, "Pacientes", "AREAS", "Áreas"),
            submodulo(MODULO_PACIENTES, "Pacientes", "HABITACIONES", "Habitaciones"),
            submodulo(MODULO_PACIENTES, "Pacientes", "CAMAS", "Camas"),
            submodulo(MODULO_PACIENTES, "Pacientes", "INSTITUCIONES", "Instituciones"),
            submodulo(MODULO_PACIENTES, "Pacientes", "PARENTESCOS", "Parentescos"),
            submodulo(MODULO_PACIENTES, "Pacientes", "ALERGIAS", "Alergias"),
            submodulo(MODULO_PACIENTES, "Pacientes", "ENFERMEDADES", "Enfermedades"),

            submodulo(MODULO_MEDICA, "Gestión médica", "MED-ATENCION", "Atención médica"),
            submodulo(MODULO_MEDICA, "Gestión médica", "MED-EVALUACION", "Evaluación clínica"),
            submodulo(MODULO_MEDICA, "Gestión médica", "MED-PROCEDIMIENTO", "Procedimiento médico"),
            submodulo(MODULO_MEDICA, "Gestión médica", "MED-INDICACIONES", "Indicaciones médicas"),
            submodulo(MODULO_MEDICA, "Gestión médica", "MED-EVOLUCION", "Evolución médica"),
            submodulo(MODULO_MEDICA, "Gestión médica", "MED-ALTA", "Alta médica"),

            submodulo(MODULO_ENFERMERIA, "Enfermería", "ENF-ATENCION", "Atención de enfermería"),
            submodulo(MODULO_ENFERMERIA, "Enfermería", "ENF-MONITOREO", "Monitoreo"),
            submodulo(MODULO_ENFERMERIA, "Enfermería", "ENF-TRATAMIENTOS", "Tratamientos"),
            submodulo(MODULO_ENFERMERIA, "Enfermería", "ENF-NOTAS", "Notas de enfermería"),

            submodulo(MODULO_CATALOGOS, "Catálogos", "CATALOGOS-FARMACIA", "Catálogos de farmacia"),
            submodulo(MODULO_CATALOGOS, "Catálogos", "CATALOGOS-PACIENTES", "Catálogos de pacientes"),
            submodulo(MODULO_CATALOGOS, "Catálogos", "CATALOGOS-USUARIOS", "Catálogos de usuarios"),
            submodulo(MODULO_CATALOGOS, "Catálogos", "CATALOGOS-CLINICA", "Catálogos de gestión clínica"),

            submodulo(MODULO_REPORTES, "Reportes", "REPORTES-FARMACIA", "Reportes de farmacia"),
            submodulo(MODULO_REPORTES, "Reportes", "REPORTES-PACIENTES", "Reportes de pacientes"),
            submodulo(MODULO_REPORTES, "Reportes", "REPORTES-USUARIOS", "Reportes de usuarios"),
            submodulo(MODULO_REPORTES, "Reportes", "REPORTES-MEDICA", "Reportes médicos"),
            submodulo(MODULO_REPORTES, "Reportes", "REPORTES-ENFERMERIA", "Reportes de enfermería"));

    private static final Map<String, List<DefinicionPermiso>> PERMISOS_POR_ROL = crearPermisosPorRol();

    private PermisosSistema() {
    }

    public record DefinicionSubmodulo(String moduloCodigo, String moduloNombre, String codigo, String nombre) {
    }

    public record DefinicionPermiso(String submoduloCodigo, boolean leer, boolean crear, boolean editar,
            boolean eliminar) {
    }

    public static List<DefinicionSubmodulo> submodulos() {
        return SUBMODULOS;
    }

    public static Map<String, List<DefinicionPermiso>> permisosPorRol() {
        return PERMISOS_POR_ROL;
    }

    public static List<String> rolesDelSistema() {
        return List.of(ROL_ADMINISTRADOR, ROL_MEDICO, ROL_ENFERMERA, ROL_DIRECTOR, ROL_FARMACEUTICO);
    }

    private static Map<String, List<DefinicionPermiso>> crearPermisosPorRol() {
        Map<String, List<DefinicionPermiso>> permisos = new LinkedHashMap<>();
        permisos.put(ROL_ADMINISTRADOR, completos(codigosDeModulo(
                MODULO_FARMACIA, MODULO_USUARIOS, MODULO_PACIENTES, MODULO_MEDICA,
                MODULO_ENFERMERIA, MODULO_CATALOGOS, MODULO_REPORTES)));

        permisos.put(ROL_DIRECTOR, concatenar(
                completos(codigosDeModulo(MODULO_FARMACIA, MODULO_USUARIOS, MODULO_PACIENTES,
                        MODULO_CATALOGOS, MODULO_REPORTES)),
                lectura(codigosDeModulo(MODULO_MEDICA, MODULO_ENFERMERIA))));

        permisos.put(ROL_MEDICO, concatenar(
                completos(union(codigosDeModulo(MODULO_MEDICA), Set.of("CATALOGOS-CLINICA"))),
                lectura(Set.of("REPORTES-MEDICA"))));

        permisos.put(ROL_ENFERMERA, concatenar(
                completos(union(codigosDeModulo(MODULO_ENFERMERIA),
                        Set.of("CATALOGOS-CLINICA", "CATALOGOS-PACIENTES"))),
                lectura(Set.of("REPORTES-ENFERMERIA"))));

        permisos.put(ROL_FARMACEUTICO, concatenar(
                completos(union(codigosDeModulo(MODULO_FARMACIA), Set.of("CATALOGOS-FARMACIA"))),
                lectura(Set.of("REPORTES-FARMACIA"))));
        return Map.copyOf(permisos);
    }

    private static DefinicionSubmodulo submodulo(String moduloCodigo, String moduloNombre, String codigo,
            String nombre) {
        return new DefinicionSubmodulo(moduloCodigo, moduloNombre, codigo, nombre);
    }

    private static Set<String> codigosDeModulo(String... modulos) {
        Set<String> filtrados = Set.of(modulos);
        return SUBMODULOS.stream()
                .filter(submodulo -> filtrados.contains(submodulo.moduloCodigo()))
                .map(DefinicionSubmodulo::codigo)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<String> union(Collection<String> primera, Collection<String> segunda) {
        Set<String> resultado = new LinkedHashSet<>(primera);
        resultado.addAll(segunda);
        return resultado;
    }

    private static List<DefinicionPermiso> completos(Collection<String> codigos) {
        return codigos.stream()
                .map(codigo -> new DefinicionPermiso(codigo, true, true, true, true))
                .toList();
    }

    private static List<DefinicionPermiso> lectura(Collection<String> codigos) {
        return codigos.stream()
                .map(codigo -> new DefinicionPermiso(codigo, true, false, false, false))
                .toList();
    }

    private static List<DefinicionPermiso> concatenar(List<DefinicionPermiso> primera,
            List<DefinicionPermiso> segunda) {
        List<DefinicionPermiso> resultado = new ArrayList<>(primera);
        resultado.addAll(segunda);
        return List.copyOf(resultado);
    }
}
