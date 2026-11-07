-- Fecha en la que se registró el procedimiento médico.
-- Se usa en el pie de la tarjeta en lugar del rango inicio-fin.
ALTER TABLE procedimientos_medicos
    ADD COLUMN fecha_registro DATETIME NULL;

-- Backfill de los procedimientos existentes con su fecha de inicio como aproximación.
UPDATE procedimientos_medicos
SET fecha_registro = inicio
WHERE fecha_registro IS NULL;

ALTER TABLE procedimientos_medicos
    MODIFY COLUMN fecha_registro DATETIME NOT NULL;
