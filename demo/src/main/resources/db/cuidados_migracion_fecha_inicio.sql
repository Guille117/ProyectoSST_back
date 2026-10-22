-- ============================================================================
-- Propósito:
--   Agregar la columna `fecha_inicio` a `cuidados`, que guarda la fecha y hora
--   en que debe iniciar el cuidado indicado.
--
-- Uso:
--   Ejecutar una sola vez sobre la base de datos `sst_db` (MySQL 8) ANTES de
--   iniciar el backend, cuando la tabla `cuidados` ya existe y contiene filas.
--   Si la tabla no existe o está vacía, Hibernate la crea/actualiza con
--   `ddl-auto=update` y este script no es necesario.
--
-- Ejecución / precauciones:
--   - Verificar primero que la columna `fecha_inicio` no exista; si ya existe,
--     no ejecutar este script.
--   - El orden agrega la columna como nullable, rellena los registros antiguos
--     con la fecha de creación y después la deja NOT NULL.
--   - No borra ni modifica otros datos.
-- ============================================================================

ALTER TABLE cuidados ADD COLUMN fecha_inicio DATETIME(6) NULL AFTER fecha;

UPDATE cuidados SET fecha_inicio = fecha WHERE fecha_inicio IS NULL;

ALTER TABLE cuidados MODIFY COLUMN fecha_inicio DATETIME(6) NOT NULL;
