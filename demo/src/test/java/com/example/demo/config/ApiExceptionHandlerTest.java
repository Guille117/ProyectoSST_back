package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleDataAccess_devuelveErrorGenericoSinDetalleInterno() {
        ResponseEntity<Map<String, Object>> response = handler.handleDataAccess(
                new DataAccessResourceFailureException("fallo interno de MySQL"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error interno del servidor", response.getBody().get("message"));
        assertFalse(response.getBody().get("message").toString().contains("MySQL"));
    }

    @Test
    void handleRuntime_conservaMensajeDeNegocio() {
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(
                new RuntimeException("Compra no encontrada"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Compra no encontrada", response.getBody().get("message"));
    }
}
