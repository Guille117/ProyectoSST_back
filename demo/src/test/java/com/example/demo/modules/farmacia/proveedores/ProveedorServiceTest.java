package com.example.demo.modules.farmacia.proveedores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository repository;

    @Mock
    private ProveedorMapper mapper;

    @InjectMocks
    private ProveedorService service;

    @Test
    void crear_debeRechazarNombreDuplicado() {
        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1234567-8",
                "12345678",
                "admin@farmacia.com",
                true
        );

        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.of(
                ProveedorEntity.builder().id(99L).nombre("Farmacia Central").estado(true).build()
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(req)
        );

        assertEquals("Ya existe un proveedor activo con el nombre: Farmacia Central", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarNitDuplicado() {
        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1234567-8",
                "12345678",
                "admin@farmacia.com",
                true
        );

        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.empty());
        when(repository.findByNitIgnoreCase("1234567-8")).thenReturn(java.util.Optional.of(
                ProveedorEntity.builder().id(99L).nit("1234567-8").estado(true).build()
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(req)
        );

        assertEquals("Ya existe un proveedor activo con el NIT: 1234567-8", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeRechazarTelefonoDuplicado() {
        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1234567-8",
                "12345678",
                "admin@farmacia.com",
                true
        );

        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.empty());
        when(repository.findByNitIgnoreCase("1234567-8")).thenReturn(java.util.Optional.empty());
        when(repository.findByTelefono("12345678")).thenReturn(java.util.Optional.of(
                ProveedorEntity.builder().id(99L).telefono("12345678").estado(true).build()
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(req)
        );

        assertEquals("Ya existe un proveedor activo con el teléfono: 12345678", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void crear_debeGuardarCuandoCamposOpcionalesVienenVacios() {
        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "",
                "",
                "",
                true
        );

        ProveedorEntity entity = new ProveedorEntity();
        entity.setNombre("Farmacia Central");
        entity.setNit(null);
        entity.setTelefono(null);
        entity.setEmail(null);

        ProveedorEntity guardado = new ProveedorEntity();
        guardado.setId(1L);
        guardado.setCodigo("PROV-01");
        guardado.setNombre("Farmacia Central");
        guardado.setNit(null);
        guardado.setTelefono(null);
        guardado.setEmail(null);

        ProveedorDTOs.Response response = new ProveedorDTOs.Response(
                1L,
                "PROV-01",
                "Farmacia Central",
                null,
                null,
                null,
                true
        );

        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any(ProveedorEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        ProveedorDTOs.Response resultado = service.crear(req);

        assertEquals(response, resultado);
        verify(repository).save(argThat(proveedor -> proveedor.getNit() == null && proveedor.getTelefono() == null && proveedor.getEmail() == null));
    }

    @Test
    void actualizar_debeRechazarNombreDuplicadoCuandoExisteProveedorActivo() {
        ProveedorEntity proveedorActual = new ProveedorEntity();
        proveedorActual.setId(1L);
        proveedorActual.setNombre("Farmacia Central");
        proveedorActual.setNit("1234567-8");
        proveedorActual.setTelefono("12345678");
        proveedorActual.setEmail("admin@farmacia.com");
        proveedorActual.setEstado(true);

        ProveedorEntity proveedorDuplicado = new ProveedorEntity();
        proveedorDuplicado.setId(2L);
        proveedorDuplicado.setNombre("Farmacia Central");
        proveedorDuplicado.setEstado(true);

        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1111111-1",
                "87654321",
                "nuevo@farmacia.com",
                true
        );

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(proveedorActual));
        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.of(proveedorDuplicado));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizar(1L, req)
        );

        assertEquals("Ya existe un proveedor activo con el nombre: Farmacia Central", exception.getMessage());
    }

    @Test
    void actualizar_debeRecomendarActivarProveedorInactivoCuandoHayDuplicado() {
        ProveedorEntity proveedorActual = new ProveedorEntity();
        proveedorActual.setId(1L);
        proveedorActual.setNombre("Farmacia Actual");
        proveedorActual.setNit("1234567-8");
        proveedorActual.setTelefono("12345678");
        proveedorActual.setEmail("admin@farmacia.com");
        proveedorActual.setEstado(true);

        ProveedorEntity proveedorDuplicado = new ProveedorEntity();
        proveedorDuplicado.setId(2L);
        proveedorDuplicado.setNombre("Farmacia Central");
        proveedorDuplicado.setEstado(false);

        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1111111-1",
                "87654321",
                "nuevo@farmacia.com",
                true
        );

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(proveedorActual));
        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.of(proveedorDuplicado));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizar(1L, req)
        );

        assertEquals("Ya existe un proveedor con el nombre: Farmacia Central pero está inactivo. Actívalo primero o actualiza ese registro inactivo.", exception.getMessage());
    }

    @Test
    void crear_debeGuardarCuandoDatosSonValidos() {
        ProveedorDTOs.Request req = new ProveedorDTOs.Request(
                "Farmacia Central",
                "1234567-8",
                "12345678",
                "admin@farmacia.com",
                true
        );

        ProveedorEntity entity = new ProveedorEntity();
        entity.setNombre("Farmacia Central");
        entity.setNit("1234567-8");
        entity.setTelefono("12345678");

        ProveedorEntity guardado = new ProveedorEntity();
        guardado.setId(1L);
        guardado.setCodigo("PROV-01");
        guardado.setNombre("Farmacia Central");
        guardado.setNit("1234567-8");
        guardado.setTelefono("12345678");

        ProveedorDTOs.Response response = new ProveedorDTOs.Response(
                1L,
                "PROV-01",
                "Farmacia Central",
                "1234567-8",
                "12345678",
                "admin@farmacia.com",
                true
        );

        when(repository.findByNombreIgnoreCase("Farmacia Central")).thenReturn(java.util.Optional.empty());
        when(repository.findByNitIgnoreCase("1234567-8")).thenReturn(java.util.Optional.empty());
        when(repository.findByTelefono("12345678")).thenReturn(java.util.Optional.empty());
        when(repository.findByEmailIgnoreCase("admin@farmacia.com")).thenReturn(java.util.Optional.empty());
        when(repository.count()).thenReturn(0L);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(any(ProveedorEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);

        ProveedorDTOs.Response resultado = service.crear(req);

        assertEquals(response, resultado);
        verify(repository).save(any(ProveedorEntity.class));
    }
}
