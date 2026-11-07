package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.puestoEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioNombreCortoTest {

    @Test
    void medicoMasculinoLlevaPrefijoDr() {
        assertEquals("Dr. Juan Perez", UsuarioNombreCorto.formatear(
                usuario("Juan", "Perez", Sexo.MASCULINO, "Medico")));
    }

    @Test
    void medicoFemeninoLlevaPrefijoDra() {
        assertEquals("Dra. Ana Lopez", UsuarioNombreCorto.formatear(
                usuario("Ana", "Lopez", Sexo.FEMENINO, "Medico")));
    }

    @Test
    void usuarioNoMedicoNoLlevaPrefijo() {
        assertEquals("Maria Lopez", UsuarioNombreCorto.formatear(
                usuario("Maria", "Lopez", Sexo.FEMENINO, "Enfermera")));
    }

    @Test
    void usaNombreCompletoCuandoFaltanLosCamposPersistidos() {
        PersonaEntity persona = PersonaEntity.builder()
                .primerNombre("Carlos")
                .segundoNombre("Alberto")
                .primerApellido("Mendez")
                .segundoApellido("Ruiz")
                .build();

        UsuarioEntity usuario = UsuarioEntity.builder()
                .username("cmendez")
                .persona(persona)
                .build();

        assertEquals("Carlos Mendez", UsuarioNombreCorto.formatear(usuario));
    }

    @Test
    void usaUsernameCuandoNoHayPersona() {
        UsuarioEntity usuario = UsuarioEntity.builder().username("sistema").build();

        assertEquals("sistema", UsuarioNombreCorto.formatear(usuario));
    }

    @Test
    void usuarioNuloDevuelveNulo() {
        assertNull(UsuarioNombreCorto.formatear(null));
    }

    private UsuarioEntity usuario(String primerNombre, String primerApellido, Sexo sexo, String puesto) {
        puestoEntity puestoEntity = new puestoEntity();
        puestoEntity.setNombre(puesto);

        PersonaEntity persona = PersonaEntity.builder()
                .primerNombre(primerNombre)
                .primerApellido(primerApellido)
                .sexo(sexo)
                .build();

        return UsuarioEntity.builder()
                .username("usuario")
                .persona(persona)
                .puesto(puestoEntity)
                .build();
    }
}
