package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.horarios.HorarioEntity;
import com.example.demo.modules.usuarios.horarios.HorarioRepository;
import com.example.demo.modules.usuarios.roles.RolEntity;
import com.example.demo.modules.usuarios.roles.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PuestoRepository puestoRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioPinRepository pinRepository;

    @InjectMocks
    private UsuarioService service;

    private UsuarioDTOs.Request buildRequest() {
        return new UsuarioDTOs.Request(
            new UsuarioDTOs.PersonaRequest(
                "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com"
            ),
                1L,
                1L,
                1L,
                "jperez",
                true
        );
    }

    @Test
    void crear_Exitoso() {
        UsuarioDTOs.Request req = buildRequest();

        PuestoEntity puesto = PuestoEntity.builder().id(1L).codigo("PUE-01").nombre("Director").build();
        HorarioEntity horario = HorarioEntity.builder().id(1L).codigo("HOR-01").nombre("Diurno").build();
        RolEntity rol = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Admin").build();

        UsuarioEntity guardado = UsuarioEntity.builder()
                .id(1L).codigo("USR-01").username("jperez").estado(true).build();
        PersonaEntity persona = PersonaEntity.builder().cui("1234567890123").nombres("Juan").apellidos("Perez").build();
        guardado.setPersona(persona);
        guardado.setPuesto(puesto);
        guardado.setHorario(horario);
        guardado.setRol(rol);

        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "12345678", "juan@test.com",
                1L, "PUE-01", "Director",
                1L, "HOR-01", "Diurno",
                1L, "ROL-01", "Admin",
                "jperez", true
        );

        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.empty());
        when(repository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.empty());
        when(puestoRepository.findById(1L)).thenReturn(Optional.of(puesto));
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(repository.count()).thenReturn(0L);
        when(repository.save(any(UsuarioEntity.class))).thenReturn(guardado);
        when(mapper.toDTO(guardado)).thenReturn(response);
        when(pinRepository.save(any(UsuarioPinEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTOs.CrearResponse resultado = service.crear(req);

        assertEquals(response, resultado.usuario());
        assertNotNull(resultado.pin());
        assertEquals(6, resultado.pin().length());
        verify(repository).save(any(UsuarioEntity.class));
        verify(pinRepository).save(any(UsuarioPinEntity.class));
    }

    @Test
    void crear_LanzaExcepcion_CuandoDatoDuplicado() {
        UsuarioDTOs.Request req = buildRequest();

        PersonaEntity personaDup = PersonaEntity.builder().cui("1234567890123").build();
        UsuarioEntity usuarioDup = UsuarioEntity.builder().id(99L).codigo("USR-99").username("otro").estado(true).persona(personaDup).build();

        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(personaDup));
        when(repository.findAll()).thenReturn(List.of(usuarioDup));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(req));
        assertTrue(ex.getMessage().contains("CUI"));
        verify(repository, never()).save(any());
    }

    @Test
    void obtenerPorId_Exitoso() {
        UsuarioEntity entity = UsuarioEntity.builder().id(1L).codigo("USR-01").username("jperez").estado(true).build();
        entity.setPersona(PersonaEntity.builder().cui("1234567890123").nombres("Juan").build());
        entity.setPuesto(PuestoEntity.builder().codigo("PUE-01").nombre("Director").build());
        entity.setHorario(HorarioEntity.builder().codigo("HOR-01").nombre("Diurno").build());
        entity.setRol(RolEntity.builder().codigo("ROL-01").nombre("Admin").build());

        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan", "Perez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), null, null,
                1L, "PUE-01", "Director",
                1L, "HOR-01", "Diurno",
                1L, "ROL-01", "Admin",
                "jperez", true
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        UsuarioDTOs.Response result = service.obtenerPorId(1L);
        assertEquals(response, result);
    }

    @Test
    void obtenerPorId_LanzaExcepcion_CuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    void actualizar_Exitoso() {
        UsuarioEntity existente = UsuarioEntity.builder()
                .id(1L).codigo("USR-01").username("jperez").password("old").estado(true).build();
        PersonaEntity persona = PersonaEntity.builder().id(10L).cui("1234567890123").nombres("Juan").apellidos("Perez").sexo(Sexo.MASCULINO).fechaNacimiento(LocalDate.of(1990, 1, 1)).build();
        existente.setPersona(persona);
        existente.setPuesto(PuestoEntity.builder().id(1L).codigo("PUE-01").nombre("Director").build());
        existente.setHorario(HorarioEntity.builder().id(1L).codigo("HOR-01").nombre("Diurno").build());
        existente.setRol(RolEntity.builder().id(1L).codigo("ROL-01").nombre("Admin").build());

        UsuarioDTOs.UpdateRequest req = new UsuarioDTOs.UpdateRequest(
            new UsuarioDTOs.PersonaRequest("1234567890123", "Juan Carlos", "Perez Lopez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "87654321", "juan2@test.com"),
            2L, 1L, 1L, "jperez2", "newpass123", "newpass123", true
        );

        PuestoEntity nuevoPuesto = PuestoEntity.builder().id(2L).codigo("PUE-02").nombre("Doctor").build();
        HorarioEntity horario = HorarioEntity.builder().id(1L).codigo("HOR-01").nombre("Diurno").build();
        RolEntity rol = RolEntity.builder().id(1L).codigo("ROL-01").nombre("Admin").build();

        UsuarioEntity actualizado = UsuarioEntity.builder().id(1L).codigo("USR-01").username("jperez2").password("newpass123").estado(true).build();
        actualizado.setPersona(persona);
        actualizado.setPuesto(nuevoPuesto);

        UsuarioDTOs.Response response = new UsuarioDTOs.Response(
                1L, "USR-01", "1234567890123", "Juan Carlos", "Perez Lopez", Sexo.MASCULINO,
                LocalDate.of(1990, 1, 1), "87654321", "juan2@test.com",
                2L, "PUE-02", "Doctor",
                1L, "HOR-01", "Diurno",
                1L, "ROL-01", "Admin",
                "jperez2", true
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(personaRepository.findByCui("1234567890123")).thenReturn(Optional.of(persona));
        // findAll will not find duplicate because same id
        when(repository.findAll()).thenReturn(List.of(existente));
        when(repository.findByUsernameIgnoreCase("jperez2")).thenReturn(Optional.empty());
        when(puestoRepository.findById(2L)).thenReturn(Optional.of(nuevoPuesto));
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("newpass123")).thenReturn("encodedNew");
        when(repository.save(any(UsuarioEntity.class))).thenReturn(actualizado);
        when(mapper.toDTO(actualizado)).thenReturn(response);

        UsuarioDTOs.Response resultado = service.actualizar(1L, req);

        assertEquals(response, resultado);
        verify(repository).save(any(UsuarioEntity.class));
    }

    @Test
    void cambiarEstado_Exitoso() {
        UsuarioEntity entity = UsuarioEntity.builder().id(1L).username("jperez").estado(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(UsuarioEntity.class))).thenReturn(entity);

        service.cambiarEstado(1L);

        assertFalse(entity.isEstado());
        verify(repository).save(entity);
    }
}
