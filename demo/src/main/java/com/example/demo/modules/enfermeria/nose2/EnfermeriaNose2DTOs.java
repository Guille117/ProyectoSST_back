package com.example.demo.modules.enfermeria.nose2;

import jakarta.validation.constraints.NotBlank;

public class EnfermeriaNose2DTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            Boolean estado
    ) {}

    public record Response(
            Long id,
            String codigo,
            String nombre,
            boolean estado
    ) {}
}
