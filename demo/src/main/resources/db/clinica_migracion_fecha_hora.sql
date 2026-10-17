-- ============================================================================
-- Propósito:
--   Eliminar la columna obsoleta `fecha_hora` de las tablas clínicas
--   (examenes_fisicos, diagnosticos, historias_clinicas).
--
--   Esa columna quedó de un diseño anterior en el que el campo se llamaba
--   `fechaHora`; luego se renombró a `fecha` (que es la que usan la entidad,
--   los DTO, el servicio y el frontend). Como Hibernate se ejecuta con
--   `ddl-auto=update`, creó la columna `fecha` pero no elimina columnas: la
--   vieja `fecha_hora` siguió existiendo como NOT NULL y sin valor por defecto.
--   Con Strict SQL Mode (STRICT_TRANS_TABLES) eso provoca que cualquier INSERT
--   falle con: "Field 'fecha_hora' doesn't have a default value".
--
-- Uso:
--   Ejecutar una vez sobre la base de datos `sst_db` (MySQL 8) que todavía
--   conserve la columna `fecha_hora`.
--
-- Ejecución / precauciones:
--   - Sin procedimientos almacenados: son sentencias ALTER TABLE directas.
--   - Ejecutar una sola vez. Si la columna ya fue eliminada (o si la base se
--     recrea desde las entidades actuales, que solo tienen `fecha`), estas
--     sentencias fallarán indicando que la columna no existe; en ese caso no
--     son necesarias.
--   - No modifica la columna `fecha`, que sigue registrando la fecha y hora
--     del servidor al crear cada registro.
-- ============================================================================

ALTER TABLE examenes_fisicos   DROP COLUMN fecha_hora;
ALTER TABLE diagnosticos       DROP COLUMN fecha_hora;
ALTER TABLE historias_clinicas DROP COLUMN fecha_hora;
