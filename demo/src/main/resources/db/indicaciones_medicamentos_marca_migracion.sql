-- =====================================================================
-- Propósito: guardar en la indicación médica la marca del medicamento del
--            catálogo al momento de indicarlo, para mostrar la descripción
--            completa (nombre, marca, presentación y vía) en las tarjetas de
--            indicaciones y de Enfermería.
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--       Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo agrega una columna nullable.
--   - Las indicaciones existentes quedan sin marca; se seguirá mostrando el
--     nombre y la presentación/vía que ya estaban guardados.
-- =====================================================================

ALTER TABLE indicaciones_medicas
    ADD COLUMN medicamento_log_marca VARCHAR(150) NULL AFTER medicamento_log_presentacion;
