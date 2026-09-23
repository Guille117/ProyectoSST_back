package com.example.demo.modules.usuarios.usuarios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NombrePersonaParserTest {

    @Test
    void separaNombresCompuestosYParticulas() {
        NombrePersonaParser.PartesNombre partes = NombrePersonaParser.separar(
                "Juan Carlos de la Cruz",
                "De los Santos López"
        );

        assertEquals("Juan", partes.primerNombre());
        assertEquals("Carlos", partes.segundoNombre());
        assertEquals("de la Cruz", partes.otrosNombres());
        assertEquals("De los Santos", partes.primerApellido());
        assertEquals("López", partes.segundoApellido());
    }

    @Test
    void dejaCamposFaltantesComoNull() {
        NombrePersonaParser.PartesNombre partes = NombrePersonaParser.separar("Juan", "López");

        assertEquals("Juan", partes.primerNombre());
        assertNull(partes.segundoNombre());
        assertNull(partes.otrosNombres());
        assertEquals("López", partes.primerApellido());
        assertNull(partes.segundoApellido());
    }
}