package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;

import jakarta.transaction.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class puestoService extends serviceBase<puestoEntity>{

    private static final List<PuestoDelSistema> PUESTOS_DEL_SISTEMA = List.of(
            new PuestoDelSistema("Administrador", List.of("administrador", "admin")),
            new PuestoDelSistema("Médico", List.of("medico", "doctor", "doctora"))
    );

    // crea una variable inmutable
    private final puestoRepository puestoRepository;

    // Constructor para inicializar el repositorio
    public puestoService(puestoRepository puestoRepository) {
        this.puestoRepository = puestoRepository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<puestoEntity> getRepository() {
        return puestoRepository;
    }

    @Transactional 
    public long contarPuestos() {
        return puestoRepository.countByEstado(true);
    }

    @Override
    public puestoEntity guardar(puestoEntity entidad) {
        validarNombreNoReservado(entidad.getNombre());
        return super.guardar(entidad);
    }

    @Override
    public Optional<puestoEntity> actualizar(Long id, String nombre) {
        Optional<puestoEntity> existente = buscarPorId(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        validarPuestoNoProtegido(existente.get());
        validarNombreNoReservado(nombre);
        return super.actualizar(id, nombre);
    }

    @Override
    public Optional<puestoEntity> cambiarEstado(Long id) {
        Optional<puestoEntity> existente = buscarPorId(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        validarPuestoNoProtegido(existente.get());
        return super.cambiarEstado(id);
    }

    private void validarPuestoNoProtegido(puestoEntity puesto) {
        PUESTOS_DEL_SISTEMA.stream()
                .filter(protegido -> esNombreSimilar(puesto.getNombre(), protegido.variantes()))
                .findFirst()
                .ifPresent(protegido -> lanzarPuestoDelSistema(protegido.nombre()));
    }

    private void validarNombreNoReservado(String nombre) {
        PUESTOS_DEL_SISTEMA.stream()
                .filter(protegido -> esNombreSimilar(nombre, protegido.variantes()))
                .findFirst()
                .ifPresent(protegido -> lanzarPuestoDelSistema(protegido.nombre()));
    }

    private static boolean esNombreSimilar(String nombre, List<String> variantes) {
        String texto = normalizar(nombre).replace(" ", "");
        String[] palabras = normalizar(nombre).split(" ");
        for (String palabra : palabras) {
            for (String variante : variantes) {
                if (texto.contains(variante)) {
                    return true;
                }
                int distanciaMaxima = variante.length() <= 6 ? 1 : 2;
                if (distancia(palabra, variante) <= distanciaMaxima) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalizar(String nombre) {
        return Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static int distancia(String primero, String segundo) {
        int[] anterior = new int[segundo.length() + 1];
        for (int indice = 0; indice <= segundo.length(); indice++) {
            anterior[indice] = indice;
        }

        for (int fila = 1; fila <= primero.length(); fila++) {
            int[] actual = new int[segundo.length() + 1];
            actual[0] = fila;
            for (int columna = 1; columna <= segundo.length(); columna++) {
                int costo = primero.charAt(fila - 1) == segundo.charAt(columna - 1) ? 0 : 1;
                actual[columna] = Math.min(
                        Math.min(actual[columna - 1] + 1, anterior[columna] + 1),
                        anterior[columna - 1] + costo);
            }
            anterior = actual;
        }
        return anterior[segundo.length()];
    }

    private static void lanzarPuestoDelSistema(String nombre) {
        throw new IllegalArgumentException(
                "El puesto '" + nombre + "' es un elemento del sistema y no se puede alterar ni duplicar.");
    }

    private record PuestoDelSistema(String nombre, List<String> variantes) {}
    
}
