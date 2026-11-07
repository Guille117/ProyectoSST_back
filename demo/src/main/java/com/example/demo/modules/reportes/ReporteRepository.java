package com.example.demo.modules.reportes;

import com.example.demo.modules.bitacora.BitacoraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consultas agregadas de reportes. Usan SQL nativo sobre las tablas existentes.
 * Se apoya en {@link BitacoraEntity} solo como entidad ancla de Spring Data; los
 * queries son independientes de ella.
 */
@Repository
public interface ReporteRepository extends JpaRepository<BitacoraEntity, Long> {

    // --- Farmacia ---

    @Query(value = """
            SELECT d.nombre, COALESCE(SUM(m.cantidad), 0)
            FROM solicitudes_salida_movimientos m
            JOIN solicitudes_salida_detalle d ON d.id = m.detalle_id
            WHERE m.tipo_movimiento = 'ENTREGA'
              AND m.fecha_hora >= :desde AND m.fecha_hora <= :hasta
            GROUP BY d.nombre
            ORDER BY SUM(m.cantidad) DESC
            """, nativeQuery = true)
    List<Object[]> despachosPorProducto(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT d.nombre, d.tipo, COALESCE(SUM(m.cantidad), 0)
            FROM solicitudes_salida_movimientos m
            JOIN solicitudes_salida_detalle d ON d.id = m.detalle_id
            WHERE m.tipo_movimiento = 'ENTREGA'
              AND m.fecha_hora >= :desde AND m.fecha_hora <= :hasta
            GROUP BY d.nombre, d.tipo
            ORDER BY SUM(m.cantidad) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> masVendidos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT d.nombre, COALESCE(SUM(m.cantidad), 0)
            FROM solicitudes_salida_movimientos m
            JOIN solicitudes_salida_detalle d ON d.id = m.detalle_id
            WHERE m.tipo_movimiento = 'ENTREGA'
              AND m.fecha_hora >= :desde AND m.fecha_hora <= :hasta
            GROUP BY d.nombre
            ORDER BY SUM(m.cantidad) ASC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> menosVendidos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT COALESCE(m.nombre, i.nombre) AS producto,
                   COALESCE(SUM(l.disponible), 0) AS cantidad,
                   MONTH(l.fecha_vencimiento) AS mes,
                   YEAR(l.fecha_vencimiento) AS anio
            FROM lotes l
            LEFT JOIN medicamentos_log m ON m.id = l.medicamento_log_id
            LEFT JOIN insumos_log i ON i.id = l.insumo_log_id
            WHERE l.fecha_vencimiento IS NOT NULL
            GROUP BY COALESCE(m.nombre, i.nombre), MONTH(l.fecha_vencimiento), YEAR(l.fecha_vencimiento)
            ORDER BY SUM(l.disponible) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> vencimientos();

    @Query(value = """
            SELECT p.nombre, COUNT(c.id), COALESCE(SUM(c.total), 0)
            FROM compras c
            JOIN proveedores p ON p.id = c.proveedor_id
            WHERE c.fecha >= :desde AND c.fecha <= :hasta
            GROUP BY p.nombre
            ORDER BY SUM(c.total) DESC
            """, nativeQuery = true)
    List<Object[]> comprasPorProveedor(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT l.id,
                   COALESCE(m.nombre, i.nombre) AS producto,
                   l.disponible,
                   l.fecha_vencimiento
            FROM lotes l
            LEFT JOIN medicamentos_log m ON m.id = l.medicamento_log_id
            LEFT JOIN insumos_log i ON i.id = l.insumo_log_id
            WHERE l.estado = true AND l.disponible <= :umbral
            ORDER BY l.disponible ASC
            """, nativeQuery = true)
    List<Object[]> stockBajo(@Param("umbral") int umbral);

    // --- Pacientes ---

    @Query(value = """
            SELECT a.nombre, c.estado, COUNT(c.id)
            FROM camas c
            JOIN areas a ON a.id = c.area_id
            WHERE c.activo = true
            GROUP BY a.nombre, c.estado
            ORDER BY a.nombre
            """, nativeQuery = true)
    List<Object[]> censoCamas();

    @Query(value = """
            SELECT a.nombre, COUNT(e.id)
            FROM episodios e
            JOIN camas c ON c.id = e.cama_id
            JOIN areas a ON a.id = c.area_id
            WHERE e.fecha_hora_ingreso >= :desde AND e.fecha_hora_ingreso <= :hasta
            GROUP BY a.nombre
            ORDER BY COUNT(e.id) DESC
            """, nativeQuery = true)
    List<Object[]> ingresosPorArea(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT p2.sexo, COUNT(p1.id)
            FROM pacientes p1
            JOIN personas p2 ON p2.id = p1.persona_id
            GROUP BY p2.sexo
            """, nativeQuery = true)
    List<Object[]> demografiaPorSexo();

    // --- Médica ---

    @Query(value = """
            SELECT d.descripcion, COUNT(d.id)
            FROM diagnosticos d
            WHERE d.fecha >= :desde AND d.fecha <= :hasta AND d.estado = true
            GROUP BY d.descripcion
            ORDER BY COUNT(d.id) DESC
            """, nativeQuery = true)
    List<Object[]> diagnosticosFrecuentes(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT a.tipo_alta, COUNT(a.id)
            FROM altas a
            WHERE a.fecha_hora_egreso >= :desde AND a.fecha_hora_egreso <= :hasta AND a.estado = 'VIGENTE'
            GROUP BY a.tipo_alta
            ORDER BY COUNT(a.id) DESC
            """, nativeQuery = true)
    List<Object[]> altasPorTipo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT u.username, COUNT(e.id)
            FROM episodios e
            JOIN usuarios u ON u.id = e.medico_id
            WHERE e.fecha_hora_ingreso >= :desde AND e.fecha_hora_ingreso <= :hasta
            GROUP BY u.username
            ORDER BY COUNT(e.id) DESC
            """, nativeQuery = true)
    List<Object[]> atencionesPorMedico(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // --- Enfermería ---

    @Query(value = """
            SELECT
              (SELECT COUNT(*) FROM signos_vitales sv WHERE sv.fecha >= :desde AND sv.fecha <= :hasta AND sv.eliminado = false),
              (SELECT COUNT(*) FROM glucosa g WHERE g.fecha >= :desde AND g.fecha <= :hasta AND g.eliminado = false),
              (SELECT COUNT(*) FROM control_hidrico ch WHERE ch.fecha >= :desde AND ch.fecha <= :hasta AND ch.eliminado = false)
            """, nativeQuery = true)
    List<Object[]> monitoreoResumen(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT COALESCE(m.nombre, im.medicamento_libre), COUNT(am.id)
            FROM aplicaciones_medicamento am
            JOIN indicaciones_medicas im ON im.id = am.indicacion_medicamento_id
            LEFT JOIN medicamentos_log m ON m.id = im.medicamento_log_id
            WHERE am.estado = 'ADMINISTRADO' AND am.eliminado = false
              AND am.fecha_registro >= :desde AND am.fecha_registro <= :hasta
            GROUP BY COALESCE(m.nombre, im.medicamento_libre)
            ORDER BY COUNT(am.id) DESC
            """, nativeQuery = true)
    List<Object[]> tratamientosMedicamentos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT c.nombre_cuidado, COUNT(ec.id)
            FROM ejecuciones_cuidado ec
            JOIN cuidados c ON c.id = ec.cuidado_id
            WHERE ec.estado = 'REALIZADO' AND ec.eliminado = false
              AND ec.fecha_registro >= :desde AND ec.fecha_registro <= :hasta
            GROUP BY c.nombre_cuidado
            ORDER BY COUNT(ec.id) DESC
            """, nativeQuery = true)
    List<Object[]> tratamientosCuidados(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT g.tipo, AVG(g.glucosa)
            FROM glucosa g
            WHERE g.eliminado = false AND g.fecha >= :desde AND g.fecha <= :hasta
            GROUP BY g.tipo
            """, nativeQuery = true)
    List<Object[]> glucosaPromedio(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // --- Usuarios ---

    @Query(value = """
            SELECT r.nombre, COUNT(u.id)
            FROM usuarios u
            JOIN usuario_roles ur ON ur.usuario_id = u.id
            JOIN roles r ON r.id = ur.rol_id
            WHERE u.estado = true
            GROUP BY r.nombre
            ORDER BY COUNT(u.id) DESC
            """, nativeQuery = true)
    List<Object[]> usuariosPorRol();

    @Query(value = """
            SELECT b.usuario_nombre, COUNT(b.id)
            FROM bitacora b
            WHERE b.tipo = 'LOGIN' AND b.fecha_hora >= :desde AND b.fecha_hora <= :hasta
            GROUP BY b.usuario_nombre
            ORDER BY COUNT(b.id) DESC
            """, nativeQuery = true)
    List<Object[]> accesosPorUsuario(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // --- Reportes adicionales ---

    @Query(value = """
            SELECT a.nombre, COALESCE(SUM(m.cantidad), 0)
            FROM solicitudes_salida_movimientos m
            JOIN solicitudes_salida_detalle d ON d.id = m.detalle_id
            JOIN solicitudes_salida s ON s.id = d.solicitud_id
            JOIN episodios e ON e.id = s.episodio_id
            JOIN camas c ON c.id = e.cama_id
            JOIN areas a ON a.id = c.area_id
            WHERE m.tipo_movimiento = 'ENTREGA'
              AND m.fecha_hora >= :desde AND m.fecha_hora <= :hasta
            GROUP BY a.nombre
            ORDER BY SUM(m.cantidad) DESC
            """, nativeQuery = true)
    List<Object[]> consumoPorArea(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT d.nombre,
                   ROUND(COALESCE(SUM(m.cantidad), 0) / NULLIF(MAX(s.disponible), 0) * 100, 0) AS rotacion
            FROM solicitudes_salida_movimientos m
            JOIN solicitudes_salida_detalle d ON d.id = m.detalle_id
            LEFT JOIN (
                SELECT 'MEDICAMENTO' AS tipo, medicamento_log_id AS referencia_id,
                       SUM(disponible) AS disponible
                FROM lotes WHERE medicamento_log_id IS NOT NULL
                GROUP BY medicamento_log_id
                UNION ALL
                SELECT 'INSUMO', insumo_log_id, SUM(disponible)
                FROM lotes WHERE insumo_log_id IS NOT NULL
                GROUP BY insumo_log_id
            ) s ON s.tipo = d.tipo AND s.referencia_id = d.referencia_id
            WHERE m.tipo_movimiento = 'ENTREGA'
              AND m.fecha_hora >= :desde AND m.fecha_hora <= :hasta
            GROUP BY d.nombre
            HAVING MAX(s.disponible) > 0
            ORDER BY rotacion DESC
            """, nativeQuery = true)
    List<Object[]> rotacionInventario(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT a.nombre, COUNT(*)
            FROM altas al
            JOIN episodios e ON e.id = al.episodio_id
            JOIN camas c ON c.id = e.cama_id
            JOIN areas a ON a.id = c.area_id
            WHERE al.estado = 'VIGENTE'
              AND al.fecha_hora_egreso >= :desde AND al.fecha_hora_egreso <= :hasta
            GROUP BY a.nombre
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> egresosPorArea(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT CASE
                     WHEN edad < 1 THEN 'Menor de 1 año'
                     WHEN edad < 5 THEN '1 a 4 años'
                     WHEN edad < 15 THEN '5 a 14 años'
                     WHEN edad < 25 THEN '15 a 24 años'
                     WHEN edad < 35 THEN '25 a 34 años'
                     WHEN edad < 45 THEN '35 a 44 años'
                     WHEN edad < 55 THEN '45 a 54 años'
                     WHEN edad < 65 THEN '55 a 64 años'
                     ELSE '65 años o más'
                   END AS grupo,
                   COUNT(*) AS cantidad
            FROM (
                SELECT TIMESTAMPDIFF(YEAR, p.fecha_nacimiento, CURDATE()) AS edad
                FROM pacientes pa
                JOIN personas p ON p.id = pa.persona_id
                WHERE p.fecha_nacimiento IS NOT NULL
            ) t
            GROUP BY grupo
            ORDER BY MIN(edad)
            """, nativeQuery = true)
    List<Object[]> pacientesPorGrupoEdad();

    @Query(value = """
            SELECT a.nombre,
                   ROUND(AVG(TIMESTAMPDIFF(HOUR, ec.fecha_hora_inicio, ec.fecha_hora_fin) / 24), 0) AS promedio
            FROM estancias_cama ec
            JOIN camas c ON c.id = ec.cama_id
            JOIN areas a ON a.id = c.area_id
            WHERE ec.fecha_hora_fin IS NOT NULL
              AND ec.fecha_hora_inicio >= :desde AND ec.fecha_hora_inicio <= :hasta
            GROUP BY a.nombre
            ORDER BY promedio DESC
            """, nativeQuery = true)
    List<Object[]> estanciaPromedio(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT a.nombre,
                   ROUND(SUM(CASE WHEN c.estado = 'OCUPADA' THEN 1 ELSE 0 END) / COUNT(*) * 100, 0) AS porcentaje
            FROM camas c
            JOIN areas a ON a.id = c.area_id
            WHERE c.activo = 1
            GROUP BY a.nombre
            ORDER BY porcentaje DESC
            """, nativeQuery = true)
    List<Object[]> ocupacionCamas();

    @Query(value = """
            SELECT pr.nombre, COUNT(*)
            FROM procedimientos_medicos pm
            JOIN procedimientos pr ON pr.id = pm.procedimiento_id
            WHERE pm.estado = 'REALIZADO'
              AND pm.fecha_registro >= :desde AND pm.fecha_registro <= :hasta
            GROUP BY pr.nombre
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> procedimientosRealizados(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT im.tipo_duracion, COUNT(*)
            FROM indicaciones_medicas im
            WHERE im.estado <> 'ELIMINADO'
              AND im.fecha >= :desde AND im.fecha <= :hasta
            GROUP BY im.tipo_duracion
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> indicacionesPorTipo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS paciente,
                   COUNT(*) - 1 AS reingresos
            FROM episodios e
            JOIN pacientes pa ON pa.id = e.paciente_id
            JOIN personas p ON p.id = pa.persona_id
            WHERE e.fecha_hora_ingreso >= :desde AND e.fecha_hora_ingreso <= :hasta
            GROUP BY pa.id, p.primer_nombre, p.primer_apellido
            HAVING COUNT(*) > 1
            ORDER BY reingresos DESC
            """, nativeQuery = true)
    List<Object[]> reingresos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS medico,
                   ROUND(AVG(TIMESTAMPDIFF(MINUTE, pm.inicio, pm.fin)), 0) AS minutos
            FROM procedimientos_medicos pm
            JOIN usuarios u ON u.id = pm.usuario_id
            JOIN personas p ON p.id = u.persona_id
            WHERE pm.estado = 'REALIZADO' AND pm.fin IS NOT NULL
              AND pm.inicio >= :desde AND pm.inicio <= :hasta
            GROUP BY u.id, p.primer_nombre, p.primer_apellido
            ORDER BY minutos DESC
            """, nativeQuery = true)
    List<Object[]> tiempoAtencion(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT 'Sistólica' AS signo, COUNT(*) AS cantidad
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND (sistolica < 90 OR sistolica > 140)
            UNION ALL
            SELECT 'Diastólica', COUNT(*)
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND (diastolica < 60 OR diastolica > 90)
            UNION ALL
            SELECT 'Frecuencia cardíaca', COUNT(*)
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND (frecuencia_cardiaca < 60 OR frecuencia_cardiaca > 100)
            UNION ALL
            SELECT 'Frecuencia respiratoria', COUNT(*)
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND (frecuencia_respiratoria < 12 OR frecuencia_respiratoria > 20)
            UNION ALL
            SELECT 'Temperatura', COUNT(*)
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND (temperatura < 36 OR temperatura > 37.5)
            UNION ALL
            SELECT 'Saturación', COUNT(*)
            FROM signos_vitales
            WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
              AND saturacion < 92
            """, nativeQuery = true)
    List<Object[]> signosFueraRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT 'Medicamento no administrado' AS evento, COUNT(*) AS cantidad
            FROM aplicaciones_medicamento
            WHERE estado = 'NO_ADMINISTRADO' AND eliminado = 0
              AND fecha_registro >= :desde AND fecha_registro <= :hasta
            UNION ALL
            SELECT 'Cuidado no realizado', COUNT(*)
            FROM ejecuciones_cuidado
            WHERE estado = 'NO_REALIZADO' AND eliminado = 0
              AND fecha_registro >= :desde AND fecha_registro <= :hasta
            """, nativeQuery = true)
    List<Object[]> eventosAdversos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS enfermero, COUNT(*) AS total
            FROM (
                SELECT usuario_id FROM aplicaciones_medicamento
                WHERE eliminado = 0 AND fecha_registro >= :desde AND fecha_registro <= :hasta
                UNION ALL
                SELECT usuario_id FROM ejecuciones_cuidado
                WHERE eliminado = 0 AND fecha_registro >= :desde AND fecha_registro <= :hasta
                UNION ALL
                SELECT usuario_id FROM signos_vitales
                WHERE eliminado = 0 AND fecha >= :desde AND fecha <= :hasta
            ) x
            JOIN usuarios u ON u.id = x.usuario_id
            JOIN personas p ON p.id = u.persona_id
            GROUP BY u.id, p.primer_nombre, p.primer_apellido
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> cargaPorEnfermero(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT COALESCE(usuario_nombre, '—') AS usuario, COUNT(*) AS total
            FROM bitacora
            WHERE fecha_hora >= :desde AND fecha_hora <= :hasta
            GROUP BY usuario_nombre
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> actividadPorUsuario(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT DATE_FORMAT(fecha_hora, '%Y-%m') AS periodo, COUNT(*) AS cantidad
            FROM bitacora
            WHERE entidad = 'Usuario' AND accion = 'CREAR'
              AND fecha_hora >= :desde AND fecha_hora <= :hasta
            GROUP BY periodo
            ORDER BY periodo
            """, nativeQuery = true)
    List<Object[]> usuariosCreados(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT COALESCE(usuario_nombre, '—') AS usuario, COUNT(*) AS intentos
            FROM bitacora
            WHERE tipo = 'LOGIN' AND accion = 'LOGIN'
              AND detalle LIKE 'FALLIDO%'
              AND fecha_hora >= :desde AND fecha_hora <= :hasta
            GROUP BY usuario_nombre
            ORDER BY intentos DESC
            """, nativeQuery = true)
    List<Object[]> intentosFallidos(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = """
            SELECT u.username,
                   MAX(CASE WHEN b.accion = 'LOGIN' THEN b.fecha_hora END) AS ultimo_login,
                   MAX(CASE WHEN b.accion = 'LOGOUT' THEN b.fecha_hora END) AS ultimo_logout,
                   MAX(b.fecha_hora) AS ultima_actividad
            FROM usuarios u
            JOIN bitacora b ON b.usuario_id = u.id
            GROUP BY u.id, u.username
            HAVING ultimo_login IS NOT NULL
               AND (ultimo_logout IS NULL OR ultimo_login > ultimo_logout)
            ORDER BY ultima_actividad DESC
            """, nativeQuery = true)
    List<Object[]> sesionesActivas();

    @Query(value = """
            SELECT m.nombre AS modulo, s.nombre AS submodulo, r.nombre AS rol,
                   (rp.puede_leer + 0) AS leer,
                   (rp.puede_crear + 0) AS crear,
                   (rp.puede_editar + 0) AS editar,
                   (rp.puede_eliminar + 0) AS eliminar
            FROM rol_permisos rp
            JOIN roles r ON r.id = rp.rol_id
            JOIN submodulos s ON s.id = rp.submodulo_id
            JOIN modulos m ON m.id = s.modulo_id
            ORDER BY m.nombre, s.nombre, r.nombre
            """, nativeQuery = true)
    List<Object[]> permisosPorModulo();
}
