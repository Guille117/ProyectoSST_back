package com.example.demo.modules.usuarios.roles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolesDataInitializerTest {

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private SubmoduloRepository submoduloRepository;

    @Mock
    private RolRepository rolRepository;

    @Test
    void run_siembraLaMatrizNativaEsIdempotenteYDesactivaRolesAjenos() {
        Map<String, ModuloEntity> modulos = new HashMap<>();
        Map<String, SubmoduloEntity> submodulos = new HashMap<>();
        List<RolEntity> roles = new ArrayList<>();

        ModuloEntity moduloUsuarios = ModuloEntity.builder().id(1L).codigo("USUARIOS").nombre("Usuarios").build();
        modulos.put("USUARIOS", moduloUsuarios);
        SubmoduloEntity usuarios = SubmoduloEntity.builder()
                .id(10L).modulo(moduloUsuarios).codigo("USUARIOS").nombre("Usuarios").build();
        submodulos.put("USUARIOS", usuarios);
        ModuloEntity moduloAntiguo = ModuloEntity.builder().id(99L).codigo("ANTIGUO").nombre("Antiguo").build();
        SubmoduloEntity submoduloObsoleto = SubmoduloEntity.builder()
                .id(99L).modulo(moduloAntiguo).codigo("ANTIGUO").nombre("Antiguo").build();

        RolEntity administrador = RolEntity.builder()
                .id(1L).codigo("ROL-01").nombre("Administrador").estado(false)
                .permisos(new ArrayList<>(List.of(
                        RolPermisoEntity.builder().submodulo(usuarios).puedeLeer(false).build(),
                        RolPermisoEntity.builder().submodulo(submoduloObsoleto).puedeLeer(true).build())))
                .build();
        RolEntity superUsuario = RolEntity.builder()
                .id(2L).codigo("ROL-02").nombre("Super Usuario").estado(true)
                .permisos(new ArrayList<>(List.of(
                        RolPermisoEntity.builder().submodulo(usuarios).puedeLeer(true).build())))
                .build();
        RolEntity medicoExistente = RolEntity.builder()
                .id(3L).codigo("ROL-03").nombre("Medico").estado(false)
                .permisos(new ArrayList<>())
                .build();
        roles.add(administrador);
        roles.add(superUsuario);
        roles.add(medicoExistente);

        when(moduloRepository.findByCodigo(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(modulos.get(invocation.getArgument(0))));
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(invocation -> {
            ModuloEntity modulo = invocation.getArgument(0);
            if (modulo.getId() == null) {
                modulo.setId((long) modulos.size() + 1);
            }
            modulos.put(modulo.getCodigo(), modulo);
            return modulo;
        });
        long[] siguienteSubmodulo = {1000L};
        when(submoduloRepository.findByCodigo(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(submodulos.get(invocation.getArgument(0))));
        when(submoduloRepository.save(any(SubmoduloEntity.class))).thenAnswer(invocation -> {
            SubmoduloEntity submodulo = invocation.getArgument(0);
            if (submodulo.getId() == null) {
                submodulo.setId(siguienteSubmodulo[0]++);
            }
            submodulos.put(submodulo.getCodigo(), submodulo);
            return submodulo;
        });
        when(rolRepository.findByNombreIgnoreCase(anyString())).thenAnswer(invocation -> {
            String nombre = invocation.getArgument(0);
            return roles.stream().filter(rol -> rol.getNombre().equalsIgnoreCase(nombre)).findFirst();
        });
        when(rolRepository.count()).thenAnswer(invocation -> (long) roles.size());
        when(rolRepository.existsByCodigo(anyString())).thenAnswer(invocation -> {
            String codigo = invocation.getArgument(0);
            return roles.stream().anyMatch(rol -> rol.getCodigo().equals(codigo));
        });
        when(rolRepository.save(any(RolEntity.class))).thenAnswer(invocation -> {
            RolEntity rol = invocation.getArgument(0);
            if (rol.getId() == null) {
                rol.setId((long) roles.size() + 1);
            }
            if (roles.stream().noneMatch(existente -> existente.getId().equals(rol.getId()))) {
                roles.add(rol);
            }
            return rol;
        });
        when(rolRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(roles));

        RolesDataInitializer initializer = new RolesDataInitializer(moduloRepository, submoduloRepository, rolRepository);
        initializer.run();
        initializer.run();

        Set<String> rolesActivos = roles.stream()
                .filter(RolEntity::isEstado)
                .map(RolEntity::getNombre)
                .collect(Collectors.toSet());
        assertEquals(Set.of("Administrador", "Médico", "Enfermera", "Director", "Farmaceutico"), rolesActivos);
        assertTrue(administrador.isEstado());
        assertEquals(3L, medicoExistente.getId());
        assertEquals("Médico", medicoExistente.getNombre());
        assertTrue(medicoExistente.isEstado());
        assertFalse(superUsuario.isEstado());
        assertTrue(superUsuario.getPermisos().isEmpty());

        assertEquals(PermisosSistema.submodulos().size(), administrador.getPermisos().size());
        assertTrue(administrador.getPermisos().stream()
                .allMatch(permiso -> permiso.isPuedeLeer() && permiso.isPuedeCrear()
                        && permiso.isPuedeEditar() && permiso.isPuedeEliminar()));
        assertTrue(administrador.getPermisos().stream()
                .noneMatch(permiso -> permiso.getSubmodulo().getCodigo().equals("ANTIGUO")));

        RolEntity director = roles.stream()
                .filter(rol -> rol.getNombre().equals("Director"))
                .findFirst()
                .orElseThrow();
        Map<String, RolPermisoEntity> permisosDirector = director.getPermisos().stream()
                .collect(Collectors.toMap(permiso -> permiso.getSubmodulo().getCodigo(), permiso -> permiso));
        assertTrue(permisosDirector.get("MED-ATENCION").isPuedeLeer());
        assertFalse(permisosDirector.get("MED-ATENCION").isPuedeCrear());
        assertTrue(permisosDirector.get("ENF-MONITOREO").isPuedeLeer());
        assertFalse(permisosDirector.get("ENF-MONITOREO").isPuedeEditar());
        assertTrue(permisosDirector.get("PROVEEDORES").isPuedeEliminar());
    }
}
