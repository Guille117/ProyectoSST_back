package com.example.demo.modules.usuarios.usuarios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonaEntityTest {

    @Test
    void asignarNombresNormalizaYSeparaNombresCompuestos() {
        PersonaEntity persona = new PersonaEntity();

        persona.asignarNombres("  Juan Carlos de la Cruz  ", "  De los Santos Lopez  ");

        assertEquals("Juan Carlos de la Cruz", persona.getNombres());
        assertEquals("De los Santos Lopez", persona.getApellidos());
        assertEquals("Juan", persona.getPrimerNombre());
        assertEquals("Carlos", persona.getSegundoNombre());
        assertEquals("de la Cruz", persona.getOtrosNombres());
        assertEquals("De los Santos", persona.getPrimerApellido());
        assertEquals("Lopez", persona.getSegundoApellido());
    }

    @Test
    void asignarNombresReemplazaPartesAnteriores() {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres("Juan Carlos Alberto", "Perez Lopez");

        persona.asignarNombres(" Ana ", " Ruiz ");

        assertEquals("Ana", persona.getNombres());
        assertEquals("Ruiz", persona.getApellidos());
        assertEquals("Ana", persona.getPrimerNombre());
        assertNull(persona.getSegundoNombre());
        assertNull(persona.getOtrosNombres());
        assertEquals("Ruiz", persona.getPrimerApellido());
        assertNull(persona.getSegundoApellido());
    }
}