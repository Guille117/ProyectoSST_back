package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void corregirReferenciaPuesto() {
        if (!existeTabla("usuarios") || !existeTabla("puestos")) {
            return;
        }

        List<Map<String, Object>> referencias = jdbcTemplate.queryForList("""
                SELECT CONSTRAINT_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'usuarios'
                  AND COLUMN_NAME = 'puesto_id'
                  AND REFERENCED_TABLE_NAME = 'puesto'
                """);

        for (Map<String, Object> referencia : referencias) {
            String constraintName = referencia.get("CONSTRAINT_NAME").toString();
            if (!constraintName.matches("[A-Za-z0-9_]+")) {
                throw new IllegalStateException("Nombre de FK inválido: " + constraintName);
            }

            jdbcTemplate.execute("ALTER TABLE usuarios DROP FOREIGN KEY `" + constraintName + "`");
            jdbcTemplate.execute("ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_puesto FOREIGN KEY (puesto_id) REFERENCES puestos(id)");
        }
    }

    @PostConstruct
    public void permitirDatosPersonalesOpcionales() {
        if (!existeTabla("personas")) {
            return;
        }

        hacerColumnaNullable("personas", "cui", "VARCHAR(13)");
        hacerColumnaNullable("personas", "sexo", "VARCHAR(20)");
        hacerColumnaNullable("personas", "fecha_nacimiento", "DATE");
    }

    /** Las indicaciones pueden guardar un medicamento libre cuando no está en el catálogo. */
    @PostConstruct
    public void permitirMedicamentoLibreEnIndicaciones() {
        if (!existeTabla("indicaciones_medicas")) {
            return;
        }

        hacerColumnaNullable("indicaciones_medicas", "medicamento_log_id", "BIGINT");
    }

    /** Agrega y rellena la fecha de inicio de las indicaciones médicas y los cuidados. */
    @PostConstruct
    public void asegurarFechaInicioIndicaciones() {
        asegurarFechaInicio("indicaciones_medicas");
        asegurarFechaInicio("cuidados");
    }

    private void asegurarFechaInicio(String tabla) {
        if (!existeTabla(tabla)) {
            return;
        }

        Integer existeColumna = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = 'fecha_inicio'
                """, Integer.class, tabla);

        if (existeColumna == null || existeColumna == 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE " + tabla + " ADD COLUMN fecha_inicio DATETIME(6) NULL AFTER fecha");
        }

        jdbcTemplate.execute("UPDATE " + tabla + " SET fecha_inicio = fecha WHERE fecha_inicio IS NULL");
        jdbcTemplate.execute("ALTER TABLE " + tabla + " MODIFY COLUMN fecha_inicio DATETIME(6) NOT NULL");
    }

    private void hacerColumnaNullable(String tabla, String columna, String tipoSql) {
        String nulable = jdbcTemplate.queryForObject("""
                SELECT IS_NULLABLE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, String.class, tabla, columna);
        if ("NO".equalsIgnoreCase(nulable)) {
            jdbcTemplate.execute(
                    "ALTER TABLE " + tabla + " MODIFY COLUMN " + columna + " " + tipoSql + " NULL");
        }
    }

    private boolean existeTabla(String nombre) {
        Integer cantidad = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                """, Integer.class, nombre);
        return cantidad != null && cantidad > 0;
    }
}