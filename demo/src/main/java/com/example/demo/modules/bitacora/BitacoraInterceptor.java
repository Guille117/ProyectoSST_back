package com.example.demo.modules.bitacora;

import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.security.UsuarioActualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registra automáticamente en la bitácora cada operación de escritura exitosa
 * (POST/PUT/PATCH/DELETE) sobre /api/v1, sin modificar los servicios existentes.
 * Las dependencias se resuelven con {@link ObjectProvider} para que los tests
 * de tipo {@code @WebMvcTest} puedan cargar el contexto sin los servicios.
 */
@Component
public class BitacoraInterceptor implements HandlerInterceptor {

    private static final Pattern ID_EN_RUTA = Pattern.compile("/(\\d+)(?:/|$)");
    private static final Pattern PACIENTE_EN_CUERPO = Pattern.compile(
            "\"(?:pacienteId|idPaciente)\"\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final List<Clasificacion> CLASIFICACIONES = List.of(
            new Clasificacion("/api/v1/usuarios/", "USUARIOS", "Usuario", TipoBitacora.USUARIO),
            new Clasificacion("/api/v1/roles/", "USUARIOS", "Rol", TipoBitacora.USUARIO),
            new Clasificacion("/api/v1/horarios/", "USUARIOS", "Horario", TipoBitacora.USUARIO),
            new Clasificacion("/api/v1/puestos/", "USUARIOS", "Puesto", TipoBitacora.USUARIO),
            new Clasificacion("/api/v1/especialidades/", "USUARIOS", "Especialidad", TipoBitacora.USUARIO),
            new Clasificacion("/api/v1/pacientes/", "PACIENTES", "Paciente", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/episodios/", "PACIENTES", "Episodio", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/altas/", "MEDICA", "Alta", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/diagnostico/", "MEDICA", "Diagnóstico", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/evolucion/", "MEDICA", "Evolución", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/examenFisico/", "MEDICA", "Examen físico", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/historiaClinica/", "MEDICA", "Historia clínica", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/cuidados/", "MEDICA", "Cuidado", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/examenes/", "MEDICA", "Examen", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/medicamentos/", "MEDICA", "Medicamento indicado", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/procedimientosMedicos/", "MEDICA", "Procedimiento médico", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/enfermeria/", "ENFERMERIA", "Enfermería", TipoBitacora.PACIENTE),
            new Clasificacion("/api/v1/salidas/", "FARMACIA", "Salida", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/salidasDirectas/", "FARMACIA", "Salida directa", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/compras/", "FARMACIA", "Compra", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/proveedores/", "FARMACIA", "Proveedor", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/devoluciones/", "FARMACIA", "Devolución", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/bajas/", "FARMACIA", "Baja", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/lotes/", "FARMACIA", "Lote", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/medicamentoLog/", "FARMACIA", "Medicamento", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/insumosLog/", "FARMACIA", "Insumo", TipoBitacora.GENERAL),
            new Clasificacion("/api/v1/solicitudesSalida/", "FARMACIA", "Solicitud de salida", TipoBitacora.GENERAL));

    private final ObjectProvider<BitacoraService> bitacoraService;
    private final ObjectProvider<UsuarioActualService> usuarioActualService;

    public BitacoraInterceptor(ObjectProvider<BitacoraService> bitacoraService,
            ObjectProvider<UsuarioActualService> usuarioActualService) {
        this.bitacoraService = bitacoraService;
        this.usuarioActualService = usuarioActualService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        BitacoraService servicio = bitacoraService.getIfAvailable();
        UsuarioActualService usuarioService = usuarioActualService.getIfAvailable();
        if (servicio == null || usuarioService == null) {
            return;
        }
        try {
            registrarSiCorresponde(request, response, servicio, usuarioService);
        } catch (Exception ignorada) {
            // La bitácora nunca debe interrumpir la petición original.
        }
    }

    private void registrarSiCorresponde(HttpServletRequest request, HttpServletResponse response,
            BitacoraService servicio, UsuarioActualService usuarioService) {
        AccionBitacora accion = accionPara(request.getMethod());
        if (accion == null) {
            return;
        }
        int estado = response.getStatus();
        if (estado < 200 || estado >= 300) {
            return;
        }
        Optional<UsuarioEntity> usuario = usuarioService.actual();
        if (usuario.isEmpty()) {
            return;
        }

        String uri = request.getRequestURI();
        Clasificacion clasificacion = clasificar(uri);
        String cuerpo = obtenerCuerpo(request, servicio);

        servicio.registrar(BitacoraEntity.builder()
                .tipo(clasificacion.tipo())
                .accion(accion)
                .modulo(clasificacion.modulo())
                .entidad(clasificacion.entidad())
                .entidadId(extraerIdDeRuta(uri))
                .pacienteId(resolverPacienteId(uri, cuerpo))
                .usuarioId(usuario.get().getId())
                .usuarioNombre(usuario.get().getUsername())
                .detalle(cuerpo)
                .ip(obtenerIp(request))
                .build());
    }

    private AccionBitacora accionPara(String metodo) {
        return switch (metodo) {
            case "POST" -> AccionBitacora.CREAR;
            case "PUT", "PATCH" -> AccionBitacora.EDITAR;
            case "DELETE" -> AccionBitacora.ELIMINAR;
            default -> null;
        };
    }

    private Clasificacion clasificar(String uri) {
        return CLASIFICACIONES.stream()
                .filter(c -> uri.startsWith(c.prefijo()))
                .findFirst()
                .orElse(new Clasificacion("/", null, null, TipoBitacora.GENERAL));
    }

    private Long resolverPacienteId(String uri, String cuerpo) {
        if (uri.contains("/pacientes/")) {
            Matcher paciente = Pattern.compile("/pacientes/(\\d+)").matcher(uri);
            if (paciente.find()) {
                return Long.valueOf(paciente.group(1));
            }
        }
        if (cuerpo != null) {
            Matcher cuerpoMatcher = PACIENTE_EN_CUERPO.matcher(cuerpo);
            if (cuerpoMatcher.find()) {
                return Long.valueOf(cuerpoMatcher.group(1));
            }
        }
        return null;
    }

    private Long extraerIdDeRuta(String uri) {
        Matcher matcher = ID_EN_RUTA.matcher(uri);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }
        return null;
    }

    private String obtenerCuerpo(HttpServletRequest request, BitacoraService servicio) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] contenido = wrapper.getContentAsByteArray();
            if (contenido.length > 0) {
                return servicio.redactar(new String(contenido, StandardCharsets.UTF_8));
            }
        }
        return null;
    }

    private String obtenerIp(HttpServletRequest request) {
        String reenviado = request.getHeader("X-Forwarded-For");
        if (reenviado != null && !reenviado.isBlank()) {
            return reenviado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Clasificacion(String prefijo, String modulo, String entidad, TipoBitacora tipo) {
    }
}
