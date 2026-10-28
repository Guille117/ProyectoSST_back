package com.example.demo.modules.pacientes.paciente;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoSangreTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void usaCodigosCortosEnJson() throws JsonProcessingException {
        for (TipoSangre tipo : TipoSangre.values()) {
            String codigo = tipo.getCodigo();

            assertEquals("\"" + codigo + "\"", objectMapper.writeValueAsString(tipo));
            assertEquals(tipo, objectMapper.readValue("\"" + codigo + "\"", TipoSangre.class));
        }
    }

    @Test
    void rechazaValoresLargosEnJson() {
        assertThrows(JsonProcessingException.class,
                () -> objectMapper.readValue("\"A_POSITIVO\"", TipoSangre.class));
    }
}