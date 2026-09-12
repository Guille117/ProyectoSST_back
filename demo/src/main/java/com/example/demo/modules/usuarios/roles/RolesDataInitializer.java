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

        // Usuarios - usuarios, roles, horarios
        ModuloEntity usuarios = crearModulo("USUARIOS", "Usuarios");
        crearSubmodulo(usuarios, "USUARIOS", "Usuarios");
        crearSubmodulo(usuarios, "ROLES", "Roles");
        crearSubmodulo(usuarios, "HORARIOS", "Horarios");

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

        Set<Long> submodulosConPermiso = rol.getPermisos().stream()
                .map(p -> p.getSubmodulo().getId())
                .collect(Collectors.toSet());

        for (SubmoduloEntity sub : submoduloRepository.findAll()) {
            if (submodulosConPermiso.contains(sub.getId())) {
                continue;
            }
            rol.getPermisos().add(RolPermisoEntity.builder()
                    .rol(rol)
                    .submodulo(sub)
                    .puedeLeer(true)
                    .puedeCrear(true)
                    .puedeEditar(true)
                    .puedeEliminar(true)
                    .build());
        }
        rolRepository.save(rol);
    }

    private String generarCodigoRol() {
        long total = rolRepository.count();
        return String.format("ROL-%02d", total + 1);
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
