package com.example.demo.modules.farmacia.insumosLog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class insumosLogDTOs {
    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotNull(message = "La marca es obligatoria") Long marcaId,
            String detalle
    ) {}

        public record Response(Long id, String nombre, String detalle, Long marcaId, String marca) {}

    public record MarcaResponse(String nombreMarca, Long idMarca) {}

    public record ListadoResponse(Long id, String nombre, String detalle, MarcaResponse marca, boolean estado) {}
}