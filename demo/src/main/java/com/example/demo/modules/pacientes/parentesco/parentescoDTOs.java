package com.example.demo.modules.pacientes.parentesco;

import jakarta.validation.constraints.NotBlank;

public class parentescoDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            Boolean estado
    ) {}

        public record Response(Long id, String nombre, boolean estado) {}
}