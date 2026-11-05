-- =====================================================================
-- Propósito: asociar documentos del episodio a un procedimiento médico
--            (autorizaciones) o a un alta (alta médica y recetas).
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--       Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo agrega columnas e índices.
--   - Ejecutar después de existir las tablas procedimientos_medicos y altas.
-- =====================================================================

ALTER TABLE documentos_episodio
    ADD COLUMN procedimiento_medico_id BIGINT NULL AFTER estado,
    ADD COLUMN alta_id BIGINT NULL AFTER procedimiento_medico_id;

ALTER TABLE documentos_episodio
    ADD KEY idx_documentos_episodio_procedimiento (procedimiento_medico_id),
    ADD KEY idx_documentos_episodio_alta (alta_id),
    ADD CONSTRAINT fk_documentos_episodio_procedimiento
        FOREIGN KEY (procedimiento_medico_id) REFERENCES procedimientos_medicos (id),
    ADD CONSTRAINT fk_documentos_episodio_alta
        FOREIGN KEY (alta_id) REFERENCES altas (id);
