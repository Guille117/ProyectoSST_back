package com.example.demo.config;

/** Conflicto de negocio que se responde con HTTP 409 (por ejemplo, stock pendiente o cambio de identidad bloqueado). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
