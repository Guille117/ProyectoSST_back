package com.example.demo.modules.usuarios.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolesDataInitializer implements CommandLineRunner {

    private static final String ROL_SUPER_USUARIO = "Super Usuario";

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
        crearSubmodulo(pacientes, "TIPOS-CAMA", "Tipos de cama");
        crearSubmodulo(pacientes, "AREAS", "Áreas");
        crearSubmodulo(pacientes, "HABITACIONES", "Habitaciones");
        crearSubmodulo(pacientes, "CAMAS", "Camas");

        crearOActualizarRolSuperUsuario();
    }

    // Rol con acceso total (los 4 permisos en true) a todos los submódulos existentes.
    // Idempotente: si se agregan submódulos nuevos en un arranque posterior, se les otorga el permiso automáticamente.
    private void crearOActualizarRolSuperUsuario() {
        RolEntity rol = rolRepository.findByNombreIgnoreCase(ROL_SUPER_USUARIO).orElseGet(() -> {
            RolEntity nuevo = RolEntity.builder()
                    .codigo(generarCodigoRol())
                    .nombre(ROL_SUPER_USUARIO)
                    .estado(true)
                    .build();
            return rolRepository.save(nuevo);
        });

        for (SubmoduloEntity sub : submoduloRepository.findAll()) {
            RolPermisoEntity permiso = rol.getPermisos().stream()
                .filter(p -> p.getSubmodulo() != null && p.getSubmodulo().getId().equals(sub.getId()))
                .findFirst()
                .orElseGet(() -> {
                RolPermisoEntity nuevo = RolPermisoEntity.builder()
                    .rol(rol)
                    .submodulo(sub)
                    .build();
                rol.getPermisos().add(nuevo);
                return nuevo;
                });
            permiso.setPuedeLeer(true);
            permiso.setPuedeCrear(true);
            permiso.setPuedeEditar(true);
            permiso.setPuedeEliminar(true);
        }
        rolRepository.save(rol);
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
