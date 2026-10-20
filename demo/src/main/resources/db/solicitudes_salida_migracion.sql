-- ============================================================
-- solicitudes_salida_migracion.sql
-- Propósito: Crear las tablas del despacho de Farmacia → Salidas.
--            Una solicitud se genera desde un procedimiento médico
--            con medicamentos o insumos. El detalle guarda lo
--            solicitado y lo entregado; los movimientos registran
--            cada entrega o devolución por lote.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db).
--            ddl-auto=update también crea las tablas, pero este
--            script deja explícitas las FK, únicas e índices.
-- Ejecución / precauciones:
--   - Ejecutar después de existir las tablas procedimientos_medicos,
--     episodios, usuarios, lotes, motivo_bajas y solicitudes_salida.
--   - No elimina ni modifica datos existentes.
-- ============================================================

CREATE TABLE IF NOT EXISTS solicitudes_salida (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    procedimiento_medico_id BIGINT NOT NULL,
    episodio_id BIGINT NOT NULL,
    usuario_solicita_id BIGINT NOT NULL,
    fecha_solicitud DATETIME(6) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_solicitud_salida_codigo (codigo),
    UNIQUE KEY uk_solicitud_salida_procedimiento (procedimiento_medico_id),
    KEY idx_solicitud_salida_estado (estado),
    KEY idx_solicitud_salida_fecha (fecha_solicitud),
    CONSTRAINT fk_solicitud_salida_procedimiento
        FOREIGN KEY (procedimiento_medico_id) REFERENCES procedimientos_medicos (id),
    CONSTRAINT fk_solicitud_salida_episodio
        FOREIGN KEY (episodio_id) REFERENCES episodios (id),
    CONSTRAINT fk_solicitud_salida_usuario
        FOREIGN KEY (usuario_solicita_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS solicitudes_salida_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    solicitud_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    referencia_id BIGINT NOT NULL,
    nombre VARCHAR(500) NOT NULL,
    cantidad_solicitada INT NOT NULL,
    cantidad_entregada INT NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_solicitud_salida_detalle_item (solicitud_id, tipo, referencia_id),
    KEY idx_solicitud_salida_detalle_solicitud (solicitud_id),
    CONSTRAINT fk_solicitud_salida_detalle_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitudes_salida (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS solicitudes_salida_movimientos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    detalle_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    condicion VARCHAR(20) NULL,
    motivo_baja_id BIGINT NULL,
    observacion VARCHAR(500) NULL,
    usuario_id BIGINT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_solicitud_salida_mov_detalle (detalle_id),
    KEY idx_solicitud_salida_mov_lote (lote_id),
    CONSTRAINT fk_solicitud_salida_mov_detalle
        FOREIGN KEY (detalle_id) REFERENCES solicitudes_salida_detalle (id),
    CONSTRAINT fk_solicitud_salida_mov_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_solicitud_salida_mov_motivo
        FOREIGN KEY (motivo_baja_id) REFERENCES motivo_bajas (id),
    CONSTRAINT fk_solicitud_salida_mov_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
