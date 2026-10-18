-- ============================================================
-- episodios_fecha_hora_ingreso_migracion.sql
-- Propósito: Agregar la fecha y hora de ingreso del paciente al
--            episodio. Los episodios existentes se completan con
--            la fecha y hora actuales.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db).
-- ============================================================

ALTER TABLE episodios
    ADD COLUMN fecha_hora_ingreso DATETIME(6) NULL;

UPDATE episodios
SET fecha_hora_ingreso = NOW()
WHERE fecha_hora_ingreso IS NULL;

ALTER TABLE episodios
    MODIFY COLUMN fecha_hora_ingreso DATETIME(6) NOT NULL;

-- Verificación opcional:
-- SELECT id, fecha_hora_ingreso FROM episodios;
