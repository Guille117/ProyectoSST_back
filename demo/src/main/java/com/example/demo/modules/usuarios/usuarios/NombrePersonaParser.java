package com.example.demo.modules.usuarios.usuarios;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class NombrePersonaParser {

    private static final Set<String> PARTICULAS = Set.of("los", "las", "y", "de", "del", "la", "el");

    private NombrePersonaParser() {
    }

    public static PartesNombre separar(String nombres, String apellidos) {
        List<String> nombresSeparados = agrupar(nombres);
        List<String> apellidosSeparados = agrupar(apellidos);

        return new PartesNombre(
                valor(nombresSeparados, 0),
                valor(nombresSeparados, 1),
                unirDesde(nombresSeparados, 2),
                valor(apellidosSeparados, 0),
                valor(apellidosSeparados, 1)
        );
    }

    private static List<String> agrupar(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }

        String[] tokens = texto.trim().replaceAll("\\s+", " ").split(" ");
        List<String> grupos = new java.util.ArrayList<>();
        for (int indice = 0; indice < tokens.length; indice++) {
            StringBuilder grupo = new StringBuilder(tokens[indice]);
            while (PARTICULAS.contains(tokens[indice].toLowerCase(Locale.ROOT)) && indice + 1 < tokens.length) {
                indice++;
                grupo.append(' ').append(tokens[indice]);
            }
            grupos.add(grupo.toString());
        }
        return grupos;
    }

    private static String valor(List<String> valores, int indice) {
        return valores.size() > indice ? valores.get(indice) : null;
    }

    private static String unirDesde(List<String> valores, int indice) {
        return valores.size() > indice
                ? valores.subList(indice, valores.size()).stream().collect(Collectors.joining(" "))
                : null;
    }

    public record PartesNombre(
            String primerNombre,
            String segundoNombre,
            String otrosNombres,
            String primerApellido,
            String segundoApellido
    ) {
    }
}