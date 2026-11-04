-- =====================================================================
-- Propósito: crear la tabla de bitácora (auditoría de movimientos).
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación con el
--       módulo de reportes/bitácora. Con `ddl-auto=validate` Hibernate
--       no crea tablas nuevas, por lo que esta migración es necesaria.
-- Ejecución/precauciones:
--   - Ejecutar sobre la base de datos real una única vez.
--   - No es destructiva: solo crea la tabla si no existe.
--   - Orden: ejecutar antes de habilitar los endpoints de bitácora.
-- =====================================================================

CREATE TABLE IF NOT EXISTS bitacora (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    tipo           VARCHAR(20)  NOT NULL,
    accion         VARCHAR(20)  NOT NULL,
    modulo         VARCHAR(50)  NULL,
    entidad        VARCHAR(80)  NULL,
    entidad_id     BIGINT       NULL,
    paciente_id    BIGINT       NULL,
    usuario_id     BIGINT       NULL,
    usuario_nombre VARCHAR(50)  NULL,
    fecha_hora     DATETIME     NOT NULL,
    detalle        LONGTEXT     NULL,
    ip             VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    KEY idx_bitacora_fecha_hora (fecha_hora),
    KEY idx_bitacora_paciente (paciente_id),
    KEY idx_bitacora_tipo (tipo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
