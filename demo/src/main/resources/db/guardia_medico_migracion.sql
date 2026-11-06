-- Médicos sin horario y guardia médica con inicio por usuario.
-- Ejecutar en entornos con spring.jpa.hibernate.ddl-auto=validate (producción).
ALTER TABLE usuarios MODIFY horario_id BIGINT NULL;
ALTER TABLE usuarios ADD COLUMN inicio_guardia DATETIME NULL;

-- Los médicos no llevan horario: limpiar asignaciones existentes.
UPDATE usuarios u
JOIN puestos p ON u.puesto_id = p.id
SET u.horario_id = NULL,
    u.inicio_guardia = NULL
WHERE LOWER(p.nombre) LIKE '%medic%'
   OR LOWER(p.nombre) LIKE '%médic%'
   OR LOWER(p.nombre) LIKE '%doctor%';
