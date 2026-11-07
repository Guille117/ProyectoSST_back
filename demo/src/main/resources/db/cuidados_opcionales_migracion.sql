-- =====================================================================
-- Propósito: permitir registrar cuidados indicando solo el nombre. La
--            frecuencia y el tipo de duración pasan a ser opcionales; sin
--            ellos Enfermería muestra el cuidado sin programación.
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--       Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo relaja dos columnas a NULL.
--   - Los cuidados existentes conservan sus valores actuales.
-- =====================================================================

ALTER TABLE cuidados
    MODIFY COLUMN frecuencia INT NULL,
    MODIFY COLUMN tipo_duracion VARCHAR(20) NULL;
