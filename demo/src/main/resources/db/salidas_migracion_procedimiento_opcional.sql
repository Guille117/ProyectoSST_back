-- ============================================================
-- salidas_migracion_procedimiento_opcional.sql
-- Propósito: Permitir salidas de Farmacia y solicitudes de Enfermería
--            que no están ligadas a un procedimiento médico. El registro
--            queda ligado al episodio y el procedimiento pasa a ser
--            opcional. La restricción única se conserva: MySQL admite
--            varios NULL en una columna UNIQUE.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db).
-- Ejecución / precauciones:
--   - No elimina ni modifica datos existentes; solo cambia la nulabilidad.
--   - Requiere que la tabla solicitudes_salida ya exista.
--   - Ejecutar antes de usar el endpoint de salidas directas.
-- ============================================================

ALTER TABLE solicitudes_salida
    MODIFY procedimiento_medico_id BIGINT NULL;
