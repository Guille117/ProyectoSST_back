package com.example.demo.modules.farmacia.proveedores;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProveedorDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,

            // NIT Guatemala: dígitos con guion antes del dígito verificador, o CF
            @Pattern(
                regexp = "^(|CF|[0-9]+-[0-9Kk])$",
                message = "NIT inválido. Formatos aceptados: 1234567-8, 1234567-K o CF"
            )
            String nit,

            @Pattern(
                regexp = "^(|[0-9]{8})$",
                message = "El teléfono debe tener exactamente 8 dígitos numéricos"
            )
            String telefono,

            @Email(message = "Formato de email inválido")
            String email,

            Boolean estado
    ) {}

    public record Response(
            Long id,
            String codigo,
            String nombre,
            String nit,
            String telefono,
            String email,
            boolean estado
    ) {}
}
