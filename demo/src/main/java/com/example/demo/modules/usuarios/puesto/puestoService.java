package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.repositoryBase;
import com.example.demo.modules.catalogo.serviceBase;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;

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
    private final UsuarioRepository usuarioRepository;

    // Constructor para inicializar el repositorio
    public puestoService(puestoRepository puestoRepository, UsuarioRepository usuarioRepository) {
        this.puestoRepository = puestoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // indica el repositorio con que va a trabajar
    @Override
    protected repositoryBase<puestoEntity> getRepository() {
        return puestoRepository;
    }

    @Override
    protected boolean estaRelacionado(puestoEntity entidad) {
        return usuarioRepository.existsByPuesto_Id(entidad.getId());
    }

    @Override
    protected void validarPropiedadDelSistema(puestoEntity entidad) {
        validarPuestoNoProtegido(entidad);
    }

    @Transactional 
    public long contarPuestos() {
        return puestoRepository.countByEstado(true);
    }

    @Override
    public puestoEntity guardar(puestoEntity entidad) {
        return super.guardar(entidad);
    }

    @Override
    public Optional<puestoEntity> actualizar(Long id, String nombre) {
        Optional<puestoEntity> existente = buscarPorId(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        validarPuestoNoProtegido(existente.get());
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
                .filter(protegido -> esNombreReservado(puesto.getNombre(), protegido.variantes()))
                .findFirst()
                .ifPresent(protegido -> lanzarPuestoDelSistema(protegido.nombre()));
    }

    /**
     * Solo bloquea el nombre exacto del rol del sistema o sus sinónimos exactos. Nombres compuestos
     * como "Médico General" son puestos distintos y deben poder registrarse.
     */
    private static boolean esNombreReservado(String nombre, List<String> variantes) {
        String normalizado = normalizar(nombre);
        return variantes.stream().anyMatch(variante -> normalizado.equals(normalizar(variante)));
    }

    private static String normalizar(String nombre) {
        return Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static void lanzarPuestoDelSistema(String nombre) {
        throw new IllegalArgumentException(
                "El puesto '" + nombre + "' es un elemento del sistema y no se puede alterar ni duplicar.");
    }

    private record PuestoDelSistema(String nombre, List<String> variantes) {}
    
}
