package com.example.demo.modules.usuarios.roles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
        void run_asignaTodosLosPermisosAlAdministradorYDejaLosDemasRolesSinPermisos() {
        List<RolEntity> roles = new ArrayList<>();
        SubmoduloEntity usuarios = SubmoduloEntity.builder().id(10L).codigo("USUARIOS").nombre("Usuarios").build();
        SubmoduloEntity pacientes = SubmoduloEntity.builder().id(20L).codigo("PACIENTES").nombre("Pacientes").build();
        SubmoduloEntity submoduloObsoleto = SubmoduloEntity.builder().id(99L).codigo("ANTIGUO").nombre("Antiguo").build();
        RolEntity administrador = RolEntity.builder()
                .id(1L).codigo("ROL-01").nombre("Administrador").estado(false)
            .permisos(new ArrayList<>(List.of(
                RolPermisoEntity.builder().rol(null).submodulo(usuarios).puedeLeer(false).build(),
                RolPermisoEntity.builder().rol(null).submodulo(submoduloObsoleto).puedeLeer(true).build())))
                .build();
        RolEntity superUsuario = RolEntity.builder()
                .id(2L).codigo("ROL-02").nombre("Super Usuario").estado(true)
            .permisos(new ArrayList<>(List.of(
                RolPermisoEntity.builder().rol(null).submodulo(usuarios).puedeLeer(true).build())))
                .build();
        RolEntity medicoExistente = RolEntity.builder()
            .id(3L).codigo("ROL-03").nombre("Medico").estado(false)
            .permisos(new ArrayList<>())
            .build();
        roles.add(administrador);
        roles.add(superUsuario);
        roles.add(medicoExistente);

        when(moduloRepository.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(submoduloRepository.existsByCodigo(anyString())).thenReturn(true);
        when(submoduloRepository.findAll()).thenReturn(List.of(usuarios, pacientes));
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
        assertEquals(2, administrador.getPermisos().size());
        for (RolPermisoEntity permiso : administrador.getPermisos()) {
            assertTrue(permiso.isPuedeLeer());
            assertTrue(permiso.isPuedeCrear());
            assertTrue(permiso.isPuedeEditar());
            assertTrue(permiso.isPuedeEliminar());
        }
        roles.stream()
            .filter(rol -> !rol.getNombre().equals("Administrador"))
            .filter(RolEntity::isEstado)
            .forEach(rol -> assertTrue(rol.getPermisos().isEmpty()));
        assertFalse(superUsuario.isEstado());
        assertTrue(superUsuario.getPermisos().isEmpty());
    }
}