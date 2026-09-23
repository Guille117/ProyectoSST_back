package com.example.demo.modules.usuarios.usuarios;

import com.example.demo.modules.usuarios.puesto.puestoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "puesto", ignore = true)
    @Mapping(target = "horario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    UsuarioEntity toEntity(UsuarioDTOs.Request request);

    // Para update: no se mapea automáticamente
    default UsuarioDTOs.Response toDTO(UsuarioEntity entity) {
        if (entity == null) return null;
        PersonaEntity p = entity.getPersona();
        puestoEntity puesto = entity.getPuesto();
        String nombres = unirNoVacios(
            p != null ? p.getPrimerNombre() : null,
            p != null ? p.getSegundoNombre() : null,
            p != null ? p.getOtrosNombres() : null
        );
        String apellidos = unirNoVacios(
            p != null ? p.getPrimerApellido() : null,
            p != null ? p.getSegundoApellido() : null
        );
        if (nombres == null && p != null) {
            nombres = p.getNombres();
        }
        if (apellidos == null && p != null) {
            apellidos = p.getApellidos();
        }
        List<Long> rolIds = entity.getRoles() == null ? List.of() : entity.getRoles().stream()
            .map(rol -> rol.getId())
            .toList();
        List<String> roles = entity.getRoles() == null ? List.of() : entity.getRoles().stream()
            .map(rol -> rol.getNombre())
            .toList();
        return new UsuarioDTOs.Response(
                entity.getId(),
                entity.getCodigo(),
            entity.getUsername(),
                nombres,
                apellidos,
            p != null ? p.getCui() : null,
                p != null ? p.getSexo() : null,
                p != null ? p.getFechaNacimiento() : null,
                p != null ? p.getTelefono() : null,
                p != null ? p.getEmail() : null,
            puesto == null ? null : new UsuarioDTOs.PuestoResponse(puesto.getId(), puesto.getNombre()),
            entity.getHorario() == null ? null : new UsuarioDTOs.HorarioResponse(
                entity.getHorario().getId(), entity.getHorario().getNombre()),
            rolIds,
            roles,
            entity.isEstado()
        );
    }

    default UsuarioDTOs.ListResponse toListDTO(UsuarioEntity entity) {
        if (entity == null) return null;
        PersonaEntity persona = entity.getPersona();
        String primerNombre = persona != null ? persona.getPrimerNombre() : null;
        String primerApellido = persona != null ? persona.getPrimerApellido() : null;

        if (primerNombre == null || primerNombre.isBlank()) {
            primerNombre = NombrePersonaParser.separar(
                    persona != null ? persona.getNombres() : null,
                    persona != null ? persona.getApellidos() : null
            ).primerNombre();
        }
        if (primerApellido == null || primerApellido.isBlank()) {
            primerApellido = NombrePersonaParser.separar(
                    persona != null ? persona.getNombres() : null,
                    persona != null ? persona.getApellidos() : null
            ).primerApellido();
        }

        String nombreCompleto = String.join(" ",
                java.util.stream.Stream.of(primerNombre, primerApellido)
                        .filter(valor -> valor != null && !valor.isBlank())
                        .toList());

        return new UsuarioDTOs.ListResponse(
                entity.getId(),
                entity.getCodigo(),
                nombreCompleto,
                persona != null ? persona.getTelefono() : null,
                entity.getRoles() == null ? List.of() : entity.getRoles().stream()
                        .map(rol -> rol.getNombre())
                        .toList(),
                entity.isEstado()
        );
    }

    private static String unirNoVacios(String... valores) {
        String resultado = java.util.Arrays.stream(valores)
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        return resultado.isBlank() ? null : resultado;
    }
}
