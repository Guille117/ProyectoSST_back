-- =====================================================================
-- Propósito: permitir cuidados del alta médica indicando solo el nombre.
--            La frecuencia y el tipo de duración del tratamiento pasan a
--            ser opcionales para los cuidados; los medicamentos siguen
--            exigiéndolos desde el servicio.
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--       Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo relaja dos columnas a NULL.
--   - Los tratamientos existentes conservan sus valores actuales.
-- =====================================================================

ALTER TABLE alta_tratamientos
    MODIFY COLUMN frecuencia INT NULL,
    MODIFY COLUMN duracion_tipo VARCHAR(20) NULL;
