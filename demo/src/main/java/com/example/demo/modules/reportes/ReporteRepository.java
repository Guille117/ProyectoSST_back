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
}
