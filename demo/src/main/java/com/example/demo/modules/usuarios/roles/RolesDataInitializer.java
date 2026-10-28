package com.example.demo.modules.usuarios.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(1)
public class RolesDataInitializer implements CommandLineRunner {

    private final ModuloRepository moduloRepository;
    private final SubmoduloRepository submoduloRepository;
    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, ModuloEntity> modulos = crearModulos();
        Map<String, SubmoduloEntity> submodulos = crearSubmodulos(modulos);
        sincronizarRolesDelSistema(submodulos);
        desactivarRolesAjenos();
    }

    private Map<String, ModuloEntity> crearModulos() {
        Map<String, ModuloEntity> modulos = new LinkedHashMap<>();
        for (PermisosSistema.DefinicionSubmodulo definicion : PermisosSistema.submodulos()) {
            modulos.computeIfAbsent(definicion.moduloCodigo(),
                    codigo -> crearModulo(codigo, definicion.moduloNombre()));
        }
        return modulos;
    }

    private Map<String, SubmoduloEntity> crearSubmodulos(Map<String, ModuloEntity> modulos) {
        Map<String, SubmoduloEntity> submodulos = new LinkedHashMap<>();
        for (PermisosSistema.DefinicionSubmodulo definicion : PermisosSistema.submodulos()) {
            ModuloEntity modulo = modulos.get(definicion.moduloCodigo());
            submodulos.put(definicion.codigo(),
                    crearSubmodulo(modulo, definicion.codigo(), definicion.nombre()));
        }
        return submodulos;
    }

    private void sincronizarRolesDelSistema(Map<String, SubmoduloEntity> submodulos) {
        Map<String, List<PermisosSistema.DefinicionPermiso>> permisosPorRol = PermisosSistema.permisosPorRol();
        for (String nombre : PermisosSistema.rolesDelSistema()) {
            RolEntity rol = buscarOCrearRol(nombre);
            sincronizarPermisosRol(rol, permisosPorRol.getOrDefault(nombre, List.of()), submodulos);
            rolRepository.save(rol);
        }
    }

    private RolEntity buscarOCrearRol(String nombre) {
        Optional<RolEntity> existente = rolRepository.findByNombreIgnoreCase(nombre);
        if (existente.isEmpty() && PermisosSistema.ROL_MEDICO.equals(nombre)) {
            existente = rolRepository.findByNombreIgnoreCase("Medico");
        }
        RolEntity rol = existente.orElseGet(() -> RolEntity.builder()
                .codigo(generarCodigoRol())
                .nombre(nombre)
                .estado(true)
                .build());
        rol.setNombre(nombre);
        rol.setEstado(true);
        return rol;
    }

    private void sincronizarPermisosRol(RolEntity rol,
            List<PermisosSistema.DefinicionPermiso> definiciones,
            Map<String, SubmoduloEntity> submodulos) {
        if (rol.getPermisos() == null) {
            rol.setPermisos(new ArrayList<>());
        }

        Map<Long, RolPermisoEntity> permisosExistentes = rol.getPermisos().stream()
                .filter(permiso -> permiso.getSubmodulo() != null)
                .collect(Collectors.toMap(permiso -> permiso.getSubmodulo().getId(), permiso -> permiso,
                        (primero, segundo) -> primero));

        for (PermisosSistema.DefinicionPermiso definicion : definiciones) {
            SubmoduloEntity submodulo = submodulos.get(definicion.submoduloCodigo());
            if (submodulo == null) {
                continue;
            }
            RolPermisoEntity permiso = permisosExistentes.computeIfAbsent(submodulo.getId(), id -> {
                RolPermisoEntity nuevo = RolPermisoEntity.builder()
                        .rol(rol)
                        .submodulo(submodulo)
                        .build();
                rol.getPermisos().add(nuevo);
                return nuevo;
            });
            permiso.setPuedeLeer(definicion.leer());
            permiso.setPuedeCrear(definicion.crear());
            permiso.setPuedeEditar(definicion.editar());
            permiso.setPuedeEliminar(definicion.eliminar());
        }

        Set<Long> idsPermitidos = definiciones.stream()
                .map(PermisosSistema.DefinicionPermiso::submoduloCodigo)
                .map(submodulos::get)
                .filter(java.util.Objects::nonNull)
                .map(SubmoduloEntity::getId)
                .collect(Collectors.toSet());
        rol.getPermisos().removeIf(permiso -> permiso.getSubmodulo() == null
                || !idsPermitidos.contains(permiso.getSubmodulo().getId()));
    }

    private void desactivarRolesAjenos() {
        for (RolEntity rol : rolRepository.findAll()) {
            if (PermisosSistema.rolesDelSistema().stream()
                    .noneMatch(nombre -> nombre.equalsIgnoreCase(rol.getNombre()))) {
                rol.setEstado(false);
                if (rol.getPermisos() == null) {
                    rol.setPermisos(new ArrayList<>());
                } else {
                    rol.getPermisos().clear();
                }
                rolRepository.save(rol);
            }
        }
    }

    private String generarCodigoRol() {
        long siguiente = rolRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("ROL-%02d", siguiente++);
        } while (rolRepository.existsByCodigo(codigo));
        return codigo;
    }

    private ModuloEntity crearModulo(String codigo, String nombre) {
        ModuloEntity modulo = moduloRepository.findByCodigo(codigo).orElseGet(() -> ModuloEntity.builder()
                .codigo(codigo)
                .nombre(nombre)
                .estado(true)
                .build());
        modulo.setNombre(nombre);
        modulo.setEstado(true);
        return moduloRepository.save(modulo);
    }

    private SubmoduloEntity crearSubmodulo(ModuloEntity modulo, String codigo, String nombre) {
        SubmoduloEntity submodulo = submoduloRepository.findByCodigo(codigo)
                .orElseGet(() -> SubmoduloEntity.builder()
                        .modulo(modulo)
                        .codigo(codigo)
                        .nombre(nombre)
                        .estado(true)
                        .build());
        submodulo.setModulo(modulo);
        submodulo.setNombre(nombre);
        submodulo.setEstado(true);
        return submoduloRepository.save(submodulo);
    }
}
