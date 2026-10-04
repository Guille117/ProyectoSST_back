ALTER TABLE medicamentos_log DROP INDEX uk_medicamento_log_nombre;
ALTER TABLE medicamentos_log
	ADD CONSTRAINT uk_medicamento_log_identidad
	UNIQUE (nombre, dosis, unidad_medida_id, marca_id, via_admin_id, presentacion_id);