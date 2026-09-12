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

    private boolean existeTabla(String nombre) {
        Integer cantidad = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                """, Integer.class, nombre);
        return cantidad != null && cantidad > 0;
    }
}