package com.example.demo.modules.paciente.camas;

import jakarta.validation.constraints.NotBlank;

public class CamaDTOs {

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
