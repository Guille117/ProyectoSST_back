package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.utils.StringNormalizer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 13)
    private String cui;

    @Transient
    private String nombres;

    @Transient
    private String apellidos;

    @Column(name = "primer_nombre", length = 100)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 100)
    private String segundoNombre;

    @Column(name = "otros_nombres", length = 150)
    private String otrosNombres;

    @Column(name = "primer_apellido", length = 100)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    public void asignarNombres(String nombres, String apellidos) {
        this.nombres = StringNormalizer.normalizarTexto(nombres);
        this.apellidos = StringNormalizer.normalizarTexto(apellidos);

        NombrePersonaParser.PartesNombre partes = NombrePersonaParser.separar(this.nombres, this.apellidos);
        this.primerNombre = partes.primerNombre();
        this.segundoNombre = partes.segundoNombre();
        this.otrosNombres = partes.otrosNombres();
        this.primerApellido = partes.primerApellido();
        this.segundoApellido = partes.segundoApellido();
    }

    public String getNombreCompleto() {
        return Stream.of(primerNombre, segundoNombre, otrosNombres, primerApellido, segundoApellido)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }
}
