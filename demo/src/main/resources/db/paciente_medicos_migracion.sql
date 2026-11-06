-- Propósito: crear la relación de médicos asignados a cada paciente y conservar el acceso
-- de los médicos que ya tenían episodios registrados antes de este cambio.
-- Uso: ejecutar una sola vez sobre la base de datos del sistema antes de desplegar el backend.
-- Ejecución/precauciones: no es destructivo; usa CREATE TABLE IF NOT EXISTS e INSERT IGNORE.
-- Si el script se ejecuta dos veces, no duplica asignaciones ni altera datos existentes.

CREATE TABLE IF NOT EXISTS paciente_medicos (
    paciente_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (paciente_id, usuario_id),
    KEY idx_paciente_medicos_usuario (usuario_id),
    CONSTRAINT fk_paciente_medicos_paciente
        FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_paciente_medicos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB;

-- Backfill: todo médico que ya atendió a un paciente conserva acceso al paciente.
INSERT IGNORE INTO paciente_medicos (paciente_id, usuario_id)
SELECT DISTINCT e.paciente_id, e.medico_id
FROM episodios e
WHERE e.paciente_id IS NOT NULL
  AND e.medico_id IS NOT NULL;
