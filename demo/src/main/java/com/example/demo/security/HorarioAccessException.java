package com.example.demo.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class HorarioAccessException extends RuntimeException {
    public HorarioAccessException(String message) {
        super(message);
    }
}
