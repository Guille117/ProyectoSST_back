-- ============================================================================
-- Propósito:
--   Agregar la columna `fecha_inicio` a `indicaciones_medicas`, que guarda la
--   fecha y hora en que debe iniciar la indicación de medicamento.
--
-- Uso:
--   Ejecutar una sola vez sobre la base de datos `sst_db` (MySQL 8) ANTES de
--   iniciar el backend, cuando la tabla `indicaciones_medicas` ya existe y
--   contiene filas. Si la tabla no existe o está vacía, Hibernate la
--   crea/actualiza con `ddl-auto=update` y este script no es necesario.
--
-- Ejecución / precauciones:
--   - Verificar primero que la columna `fecha_inicio` no exista; si ya existe,
--     no ejecutar este script.
--   - El orden agrega la columna como nullable, rellena los registros antiguos
--     con la fecha de creación y después la deja NOT NULL.
--   - No borra ni modifica otros datos.
-- ============================================================================

ALTER TABLE indicaciones_medicas ADD COLUMN fecha_inicio DATETIME(6) NULL AFTER fecha;

UPDATE indicaciones_medicas SET fecha_inicio = fecha WHERE fecha_inicio IS NULL;

ALTER TABLE indicaciones_medicas MODIFY COLUMN fecha_inicio DATETIME(6) NOT NULL;
