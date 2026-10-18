-- ============================================================================
-- Propósito:
--   Agregar la columna `fecha` a la tabla `cuidados`, que guarda la fecha y
--   hora en que se registró cada cuidado indicado.
--
-- Uso:
--   Ejecutar una sola vez sobre la base de datos `sst_db` (MySQL 8) ANTES de
--   iniciar el backend, cuando la tabla `cuidados` ya existe y contiene filas.
--   Si la tabla no existe o está vacía, Hibernate la crea/actualiza con
--   `ddl-auto=update` y este script no es necesario.
--
-- Ejecución / precauciones:
--   - Verificar primero que la columna `fecha` no exista en `cuidados`; si ya
--     existe, no ejecutar este script.
--   - El orden agrega la columna como nullable, rellena los registros antiguos
--     con la fecha y hora actual y después la deja NOT NULL.
--   - No borra ni modifica otros datos.
-- ============================================================================

ALTER TABLE cuidados ADD COLUMN fecha DATETIME(6) NULL AFTER usuario_id;

UPDATE cuidados SET fecha = NOW(6) WHERE fecha IS NULL;

ALTER TABLE cuidados MODIFY COLUMN fecha DATETIME(6) NOT NULL;
