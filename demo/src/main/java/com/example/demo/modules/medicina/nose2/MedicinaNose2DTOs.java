package com.example.demo.modules.medicina.nose2;

import jakarta.validation.constraints.NotBlank;

public class MedicinaNose2DTOs {

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
