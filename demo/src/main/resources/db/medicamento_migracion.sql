-- =====================================================================
-- Propósito: reglas de negocio del catálogo de medicamentos e insumos.
--   1. Snapshot del medicamento en la indicación médica (nombre, dosis,
--      unidad, presentación y vía) para que las correcciones posteriores
--      del catálogo no alteren el historial clínico.
--   2. Tablas de auditoría de correcciones justificadas del nombre.
-- Uso: ejecutar UNA sola vez antes de arrancar con `ddl-auto=validate`.
--       No es destructiva (agrega columnas y crea tablas si no existen).
-- =====================================================================

-- 1. Snapshot del catálogo en las indicaciones médicas -------------------
ALTER TABLE indicaciones_medicas
    ADD COLUMN medicamento_log_nombre       VARCHAR(150)  NULL,
    ADD COLUMN medicamento_log_dosis        DECIMAL(12,4) NULL,
    ADD COLUMN medicamento_log_unidad       VARCHAR(50)   NULL,
    ADD COLUMN medicamento_log_presentacion VARCHAR(100)  NULL,
    ADD COLUMN medicamento_log_via_admin    VARCHAR(100)  NULL;

-- Relleno histórico: copia los valores actuales del catálogo en las
-- indicaciones que ya existían. Las indicaciones libres no se tocan.
UPDATE indicaciones_medicas i
    JOIN medicamentos_log m ON m.id = i.medicamento_log_id
    LEFT JOIN unidad_medidas u ON u.id = m.unidad_medida_id
    LEFT JOIN presentaciones p ON p.id = m.presentacion_id
    LEFT JOIN vias_admin v ON v.id = m.via_admin_id
SET i.medicamento_log_nombre       = m.nombre,
    i.medicamento_log_dosis        = m.dosis,
    i.medicamento_log_unidad       = u.abreviatura,
    i.medicamento_log_presentacion = p.nombre,
    i.medicamento_log_via_admin    = v.nombre
WHERE i.medicamento_log_id IS NOT NULL;

-- 2. Auditoría de correcciones de medicamentos ---------------------------
CREATE TABLE IF NOT EXISTS medicamentos_correcciones (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    medicamento_log_id BIGINT       NOT NULL,
    nombre_anterior    VARCHAR(150) NOT NULL,
    nombre_nuevo       VARCHAR(150) NOT NULL,
    motivo             VARCHAR(500) NOT NULL,
    usuario_id         BIGINT       NOT NULL,
    fecha_hora         DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_med_corr_medicamento (medicamento_log_id),
    KEY idx_med_corr_usuario (usuario_id),
    CONSTRAINT fk_med_corr_medicamento FOREIGN KEY (medicamento_log_id) REFERENCES medicamentos_log (id),
    CONSTRAINT fk_med_corr_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3. Auditoría de correcciones de insumos --------------------------------
CREATE TABLE IF NOT EXISTS insumos_correcciones (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    insumo_log_id   BIGINT       NOT NULL,
    nombre_anterior VARCHAR(150) NOT NULL,
    nombre_nuevo    VARCHAR(150) NOT NULL,
    motivo          VARCHAR(500) NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    fecha_hora      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ins_corr_insumo (insumo_log_id),
    KEY idx_ins_corr_usuario (usuario_id),
    CONSTRAINT fk_ins_corr_insumo FOREIGN KEY (insumo_log_id) REFERENCES insumos_log (id),
    CONSTRAINT fk_ins_corr_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
