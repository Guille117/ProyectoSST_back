package com.example.demo.modules.farmacia.proveedores;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProveedorDTOs {

    public record Request(
            @NotBlank(message = "El nombre es obligatorio") String nombre,

            // NIT: CF, 8 dígitos sin guion, o 7-8 dígitos con guion y verificador numérico/K
            @Pattern(
                regexp = "(?i)^\\s*(|CF|[0-9]{8}|[0-9]{7,8}-[0-9Kk])\\s*$",
                message = "NIT inválido. Use CF, 8 dígitos sin guion, o 7 u 8 dígitos con guion y verificador numérico/K"
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
