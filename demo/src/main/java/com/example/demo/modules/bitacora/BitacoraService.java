package com.example.demo.modules.bitacora;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BitacoraService {

    private static final int MAX_DETALLE = 4000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final Set<String> CAMPOS_SENSIBLES = Set.of(
            "password", "passwd", "pin", "token", "secret", "clave",
            "contrasena", "contraseña", "authorization", "autorizacion");

    private final BitacoraRepository bitacoraRepository;

    /**
     * Registra una entrada en transacción independiente para que un fallo en la bitácora
     * nunca revierta la operación principal.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(BitacoraEntity entrada) {
        if (entrada.getFechaHora() == null) {
            entrada.setFechaHora(LocalDateTime.now());
        }
        bitacoraRepository.save(entrada);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogin(Long usuarioId, String usuarioNombre) {
        registrar(BitacoraEntity.builder()
                .tipo(TipoBitacora.LOGIN)
                .accion(AccionBitacora.LOGIN)
                .modulo("SEGURIDAD")
                .entidad("Sesión")
                .usuarioId(usuarioId)
                .usuarioNombre(usuarioNombre)
                .fechaHora(LocalDateTime.now())
                .ip(obtenerIp())
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogout(Long usuarioId, String usuarioNombre) {
        registrar(BitacoraEntity.builder()
                .tipo(TipoBitacora.LOGIN)
                .accion(AccionBitacora.LOGOUT)
                .modulo("SEGURIDAD")
                .entidad("Sesión")
                .usuarioId(usuarioId)
                .usuarioNombre(usuarioNombre)
                .fechaHora(LocalDateTime.now())
                .detalle("Cierre de sesión")
                .ip(obtenerIp())
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(String usuarioNombre) {
        registrar(BitacoraEntity.builder()
                .tipo(TipoBitacora.LOGIN)
                .accion(AccionBitacora.LOGIN)
                .modulo("SEGURIDAD")
                .entidad("Sesión")
                .usuarioNombre(usuarioNombre != null && !usuarioNombre.isBlank() ? usuarioNombre : "—")
                .fechaHora(LocalDateTime.now())
                .detalle("FALLIDO: credenciales inválidas")
                .ip(obtenerIp())
                .build());
    }

    @Transactional(readOnly = true)
    public List<BitacoraDTOs.BitacoraResponse> listar(Collection<TipoBitacora> tipos, Long pacienteId,
            Long usuarioId, LocalDateTime desde, LocalDateTime hasta, int limite) {
        int tamano = Math.min(Math.max(limite, 1), 500);
        return bitacoraRepository
                .filtrar(tipos, pacienteId, usuarioId, desde, hasta, PageRequest.of(0, tamano))
                .stream()
                .map(this::mapear)
                .toList();
    }

    public String redactar(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode nodo = OBJECT_MAPPER.readTree(json);
            JsonNode redactado = redactarNodo(nodo);
            String resultado = OBJECT_MAPPER.writeValueAsString(redactado);
            return resultado.length() > MAX_DETALLE ? resultado.substring(0, MAX_DETALLE) : resultado;
        } catch (Exception ex) {
            return json.length() > MAX_DETALLE ? json.substring(0, MAX_DETALLE) : json;
        }
    }

    private JsonNode redactarNodo(JsonNode nodo) {
        if (nodo.isObject()) {
            ObjectNode objeto = OBJECT_MAPPER.createObjectNode();
            nodo.fields().forEachRemaining(entrada -> {
                String clave = entrada.getKey().toLowerCase(Locale.ROOT);
                if (CAMPOS_SENSIBLES.contains(clave)) {
                    objeto.put(entrada.getKey(), "***");
                } else {
                    objeto.set(entrada.getKey(), redactarNodo(entrada.getValue()));
                }
            });
            return objeto;
        }
        if (nodo.isArray()) {
            ArrayNode arreglo = OBJECT_MAPPER.createArrayNode();
            nodo.forEach(hijo -> arreglo.add(redactarNodo(hijo)));
            return arreglo;
        }
        return nodo;
    }

    private BitacoraDTOs.BitacoraResponse mapear(BitacoraEntity entidad) {
        return new BitacoraDTOs.BitacoraResponse(
                entidad.getId(),
                entidad.getTipo() != null ? entidad.getTipo().name() : null,
                entidad.getAccion() != null ? entidad.getAccion().name() : null,
                entidad.getModulo(),
                entidad.getEntidad(),
                entidad.getEntidadId(),
                entidad.getPacienteId(),
                entidad.getUsuarioId(),
                entidad.getUsuarioNombre(),
                entidad.getFechaHora(),
                entidad.getDetalle(),
                entidad.getIp());
    }

    private String obtenerIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            HttpServletRequest request = atributos.getRequest();
            String reenviado = request.getHeader("X-Forwarded-For");
            if (reenviado != null && !reenviado.isBlank()) {
                return reenviado.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}
