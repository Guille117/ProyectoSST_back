package com.example.demo.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RegistroPermisosApi {

    private static final Set<HttpMethod> LECTURA = Set.of(HttpMethod.GET, HttpMethod.HEAD);
    private static final Set<HttpMethod> CREACION = Set.of(HttpMethod.POST);
    private static final Set<HttpMethod> ACTUALIZACION = Set.of(HttpMethod.PUT, HttpMethod.PATCH);
    private static final Set<HttpMethod> ELIMINACION = Set.of(HttpMethod.DELETE);
    private static final Set<HttpMethod> ESCRITURA = Set.of(
            HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE);

    private final PathMatcher matcher = new AntPathMatcher();
    private final List<ReglaPermiso> reglas = crearReglas();

    public Optional<ReglaPermiso> resolver(String ruta, String metodo) {
        HttpMethod httpMethod;
        try {
            httpMethod = HttpMethod.valueOf(metodo);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return reglas.stream()
                .filter(regla -> regla.metodos().contains(httpMethod))
                .filter(regla -> matcher.match(regla.patron(), ruta))
                .findFirst();
    }

    private List<ReglaPermiso> crearReglas() {
        return List.of(
                regla("/api/v1/usuarios/medicos", LECTURA,
                        modulos("USUARIOS", "PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),

                regla("/api/v1/usuarios/**", LECTURA, modulos("USUARIOS"), Set.of()),
                regla("/api/v1/usuarios/**", ESCRITURA, modulos("USUARIOS"), Set.of()),
                regla("/api/v1/roles/**", LECTURA, modulos("USUARIOS"), Set.of()),
                regla("/api/v1/horarios/**", LECTURA, modulos("USUARIOS"), Set.of()),
                regla("/api/v1/horarios/**", ESCRITURA, modulos("USUARIOS"), Set.of()),

                regla("/api/v1/puestos/**", LECTURA, modulos("USUARIOS"), submodulos("CATALOGOS-USUARIOS")),
                regla("/api/v1/puestos/**", ESCRITURA, modulos("USUARIOS"), submodulos("CATALOGOS-USUARIOS")),
                regla("/api/v1/especialidades/**", LECTURA, modulos("USUARIOS"),
                        submodulos("CATALOGOS-USUARIOS")),
                regla("/api/v1/especialidades/**", ESCRITURA, modulos("USUARIOS"),
                        submodulos("CATALOGOS-USUARIOS")),

                regla("/api/v1/proveedores/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/proveedores/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/compras/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/compras/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/salidas/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/salidas/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/salidasDirectas/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/salidasDirectas/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/solicitudesSalida/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/solicitudesSalida/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/devoluciones/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/devoluciones/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/lotes/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/lotes/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/bajas/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/bajas/**", ESCRITURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/insumosLog/**", LECTURA, modulos("FARMACIA"), Set.of()),
                regla("/api/v1/insumosLog/**", ESCRITURA, modulos("FARMACIA"), Set.of()),

                regla("/api/v1/medicamentoLog/**", LECTURA,
                        modulos("FARMACIA", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/medicamentoLog/**", ESCRITURA, modulos("FARMACIA"), Set.of()),

                regla("/api/v1/unidadMedida/**", LECTURA,
                        modulos("FARMACIA", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/unidadMedida/**", ESCRITURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/marca/**", LECTURA, modulos("FARMACIA"), submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/marca/**", ESCRITURA, modulos("FARMACIA"), submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/viaAdmin/**", LECTURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/viaAdmin/**", ESCRITURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/presentacion/**", LECTURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/presentacion/**", ESCRITURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/motivoBaja/**", LECTURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),
                regla("/api/v1/motivoBaja/**", ESCRITURA, modulos("FARMACIA"),
                        submodulos("CATALOGOS-FARMACIA")),

                regla("/api/v1/documentos/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/documentos/**", ESCRITURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/episodios/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/episodios/**", CREACION,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/pacientes/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/pacientes/**", ESCRITURA, modulos("PACIENTES"), Set.of()),

                regla("/api/v1/tiposCama/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/tiposCama/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/tipos-cama/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/tipos-cama/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/areas/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/areas/**", ESCRITURA, modulos("PACIENTES"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/habitaciones/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/habitaciones/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/instituciones/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/instituciones/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/parentescos/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/parentescos/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/alergias/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/alergias/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/enfermedades/**", LECTURA,
                        modulos("PACIENTES", "MEDICA", "ENFERMERIA"), submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/enfermedades/**", ESCRITURA, modulos("PACIENTES"),
                        submodulos("CATALOGOS-PACIENTES")),
                regla("/api/v1/camas/**", LECTURA, modulos("PACIENTES"), Set.of()),
                regla("/api/v1/camas/**", ESCRITURA, modulos("PACIENTES"), Set.of()),

                regla("/api/v1/altas/**", LECTURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/altas/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/diagnostico/**", LECTURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/diagnostico/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/evolucion/**", LECTURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/evolucion/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/examenFisico/**", LECTURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/examenFisico/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/historiaClinica/**", LECTURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/historiaClinica/**", ESCRITURA, modulos("MEDICA"), Set.of()),

                regla("/api/v1/cuidados/**", LECTURA, modulos("MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/cuidados/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/examenes/**", LECTURA, modulos("MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/examenes/**", ESCRITURA, modulos("MEDICA"), Set.of()),
                regla("/api/v1/medicamentos/**", LECTURA, modulos("MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/medicamentos/**", ESCRITURA, modulos("MEDICA"), Set.of()),

                regla("/api/v1/procedimientosMedicos/**", LECTURA,
                        modulos("MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/procedimientosMedicos/**", CREACION,
                        modulos("MEDICA", "ENFERMERIA"), Set.of()),
                regla("/api/v1/procedimientosMedicos/**", ACTUALIZACION, modulos("MEDICA"), Set.of()),
                regla("/api/v1/procedimientosMedicos/**", ELIMINACION, modulos("MEDICA"), Set.of()),

                regla("/api/v1/partesCuerpo/**", LECTURA, modulos("MEDICA", "ENFERMERIA"),
                        submodulos("CATALOGOS-CLINICA")),
                regla("/api/v1/partesCuerpo/**", ESCRITURA, modulos("MEDICA"),
                        submodulos("CATALOGOS-CLINICA")),
                regla("/api/v1/condicionesEgreso/**", LECTURA, modulos("MEDICA", "ENFERMERIA"),
                        submodulos("CATALOGOS-CLINICA")),
                regla("/api/v1/condicionesEgreso/**", ESCRITURA, modulos("MEDICA"),
                        submodulos("CATALOGOS-CLINICA")),
                regla("/api/v1/procedimientos/**", LECTURA, modulos("MEDICA", "ENFERMERIA"),
                        submodulos("CATALOGOS-CLINICA")),
                regla("/api/v1/procedimientos/**", ESCRITURA, modulos("MEDICA"),
                        submodulos("CATALOGOS-CLINICA")),

                regla("/api/v1/enfermeria/**", LECTURA, modulos("ENFERMERIA", "MEDICA"), Set.of()),
                regla("/api/v1/enfermeria/**", ESCRITURA, modulos("ENFERMERIA"), Set.of()));
    }

    private ReglaPermiso regla(String patron, Set<HttpMethod> metodos, Set<String> modulos,
            Set<String> submodulos) {
        return new ReglaPermiso(patron, metodos, modulos, submodulos);
    }

    private Set<String> modulos(String... modulos) {
        return Set.of(modulos);
    }

    private Set<String> submodulos(String... submodulos) {
        return Set.of(submodulos);
    }
}
