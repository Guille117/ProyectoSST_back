package com.example.demo.security;

import org.springframework.http.HttpMethod;

import java.util.Set;

public record ReglaPermiso(
        String patron,
        Set<HttpMethod> metodos,
        Set<String> modulos,
        Set<String> submodulos) {
}
