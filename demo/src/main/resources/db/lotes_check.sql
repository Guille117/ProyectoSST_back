-- ============================================================
-- Migración: submódulo LOTES + rework de COMPRAS
-- Ejecutar manualmente en MySQL (ddl-auto=update no genera
-- constraints CHECK ni elimina columnas).
-- ============================================================

-- 1) Restricción XOR: un lote pertenece a un insumo O a un medicamento
ALTER TABLE lotes
ADD CONSTRAINT chk_tipo_item
CHECK (
    (insumo_log_id IS NOT NULL AND medicamento_log_id IS NULL)
    OR
    (insumo_log_id IS NULL AND medicamento_log_id IS NOT NULL)
);

-- 2) Rework de compras: se eliminó el placeholder 'nombre' (era NOT NULL)
--    y se agregaron 'total' y 'comprobante'.
--    NOTA: si la tabla compras tenía datos reales previos, revisar antes.
ALTER TABLE compras DROP COLUMN nombre;
-- 'total' y 'comprobante' los crea Hibernate con ddl-auto=update.

-- Nota: la restricción UNIQUE (codigo_lote, compra_id) la genera
-- Hibernate automáticamente como uk_lote_codigo_compra.
-- La columna compra_id de lotes es NOT NULL (la crea Hibernate).
