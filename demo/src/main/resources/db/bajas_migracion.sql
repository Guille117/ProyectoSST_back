-- ============================================================
-- bajas_migracion.sql
-- Propósito: Crear la tabla `bajas`, que registra cada baja de
--            unidades de un lote. Al registrar una baja se descuenta
--            `cantidad` del stock disponible del lote.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db). ddl-auto=update
--            crea la tabla si no existe, pero no las constraints FK.
-- ============================================================

CREATE TABLE IF NOT EXISTS bajas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    lote_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    motivo_baja_id BIGINT NOT NULL,
    observacion VARCHAR(500) NULL,
    fecha_hora DATETIME NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bajas_lote FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_bajas_motivo FOREIGN KEY (motivo_baja_id) REFERENCES motivo_bajas (id),
    CONSTRAINT fk_bajas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- Verificación opcional:
-- SHOW CREATE TABLE bajas;
