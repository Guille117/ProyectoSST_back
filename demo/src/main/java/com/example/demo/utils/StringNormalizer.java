package com.example.demo.utils;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static String normalizarNullable(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
