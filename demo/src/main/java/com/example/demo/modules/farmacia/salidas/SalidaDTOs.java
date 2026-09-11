package com.example.demo.modules.farmacia.salidas;

import jakarta.validation.constraints.NotBlank;

public class SalidaDTOs {

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
