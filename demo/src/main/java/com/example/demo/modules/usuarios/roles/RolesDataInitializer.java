package com.example.demo.modules.usuarios.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
public class RolesDataInitializer implements CommandLineRunner {

    private static final List<String> ROLES_DEL_SISTEMA = List.of(
            "Administrador", "Médico", "Enfermera", "Director", "Farmaceutico");

    private final ModuloRepository moduloRepository;
    private final SubmoduloRepository submoduloRepository;
    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Farmacia - proveedores, compras, salidas, devoluciones
        ModuloEntity farmacia = crearModulo("FARMACIA", "Farmacia");
        crearSubmodulo(farmacia, "PROVEEDORES", "Proveedores");
        crearSubmodulo(farmacia, "COMPRAS", "Compras");
        crearSubmodulo(farmacia, "SALIDAS", "Salidas");
        crearSubmodulo(farmacia, "DEVOLUCIONES", "Devoluciones");
        crearSubmodulo(farmacia, "UNIDADMEDIDA", "Unidades de medida");

        // Usuarios - usuarios, roles, horarios
        ModuloEntity usuarios = crearModulo("USUARIOS", "Usuarios");
        crearSubmodulo(usuarios, "USUARIOS", "Usuarios");
        crearSubmodulo(usuarios, "ROLES", "Roles");
        crearSubmodulo(usuarios, "HORARIOS", "Horarios");

        // Pacientes - catálogos de tipo de cama y área
        ModuloEntity pacientes = crearModulo("PACIENTES", "Pacientes");
        crearSubmodulo(pacientes, "PACIENTES", "Pacientes");
        crearSubmodulo(pacientes, "TIPOS-CAMA", "Tipos de cama");
        crearSubmodulo(pacientes, "AREAS", "Áreas");
        crearSubmodulo(pacientes, "HABITACIONES", "Habitaciones");
        crearSubmodulo(pacientes, "CAMAS", "Camas");

        crearRolesDelSistema();
    }

    private void crearRolesDelSistema() {
        List<SubmoduloEntity> submodulos = submoduloRepository.findAll();

        for (String nombre : ROLES_DEL_SISTEMA) {
                RolEntity rol = rolRepository.findByNombreIgnoreCase(nombre)
                    .or(() -> nombre.equals("Médico") ? rolRepository.findByNombreIgnoreCase("Medico") : java.util.Optional.empty())
                    .orElseGet(() -> RolEntity.builder()
                    .codigo(generarCodigoRol())
                    .nombre(nombre)
                    .estado(true)
                    .build());
            rol.setNombre(nombre);
            rol.setEstado(true);
            if (nombre.equalsIgnoreCase("Administrador")) {
                sincronizarPermisosAdministrador(rol, submodulos);
            } else {
                limpiarPermisos(rol);
            }
            rolRepository.save(rol);
        }

        for (RolEntity rol : rolRepository.findAll()) {
            if (ROLES_DEL_SISTEMA.stream().noneMatch(nombre -> nombre.equalsIgnoreCase(rol.getNombre()))) {
                rol.setEstado(false);
                limpiarPermisos(rol);
                rolRepository.save(rol);
            }
        }
    }

    private void sincronizarPermisosAdministrador(RolEntity administrador, List<SubmoduloEntity> submodulos) {
        if (administrador.getPermisos() == null) {
            administrador.setPermisos(new ArrayList<>());
        }

        for (SubmoduloEntity submodulo : submodulos) {
            RolPermisoEntity permiso = administrador.getPermisos().stream()
                    .filter(actual -> actual.getSubmodulo() != null
                            && actual.getSubmodulo().getId().equals(submodulo.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        RolPermisoEntity nuevo = RolPermisoEntity.builder()
                                .rol(administrador)
                                .submodulo(submodulo)
                                .build();
                        administrador.getPermisos().add(nuevo);
                        return nuevo;
                    });
            permiso.setPuedeLeer(true);
            permiso.setPuedeCrear(true);
            permiso.setPuedeEditar(true);
            permiso.setPuedeEliminar(true);
        }

        administrador.getPermisos().removeIf(permiso -> permiso.getSubmodulo() == null
                || submodulos.stream().noneMatch(submodulo -> submodulo.getId().equals(permiso.getSubmodulo().getId())));
    }

    private void limpiarPermisos(RolEntity rol) {
        if (rol.getPermisos() == null) {
            rol.setPermisos(new ArrayList<>());
        } else {
            rol.getPermisos().clear();
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
        return moduloRepository.findByCodigo(codigo).orElseGet(() -> {
            ModuloEntity modulo = ModuloEntity.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .estado(true)
                    .build();
            return moduloRepository.save(modulo);
        });
    }

    private void crearSubmodulo(ModuloEntity modulo, String codigo, String nombre) {
        if (submoduloRepository.existsByCodigo(codigo)) {
            return;
        }
        SubmoduloEntity sub = SubmoduloEntity.builder()
                .modulo(modulo)
                .codigo(codigo)
                .nombre(nombre)
                .estado(true)
                .build();
        submoduloRepository.save(sub);
    }
}
