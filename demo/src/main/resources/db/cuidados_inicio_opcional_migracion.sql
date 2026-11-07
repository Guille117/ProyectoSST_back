-- =====================================================================
-- Propósito: permitir registrar atenciones (cuidados) solo con el nombre.
--            La fecha y hora de inicio pasa a ser opcional; Enfermería
--            puede registrar la ejecución aunque no exista programación.
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--      Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo relaja la columna a NULL.
--   - Los cuidados existentes conservan sus valores actuales.
-- =====================================================================

ALTER TABLE cuidados
    MODIFY COLUMN fecha_inicio DATETIME NULL;
