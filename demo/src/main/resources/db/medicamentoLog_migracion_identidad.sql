-- ============================================================
-- Propósito: Migración de la restricción UNIQUE de medicamentos_log
-- Antes: la unicidad era solo por nombre (uk_medicamento_log_nombre).
-- Después: la identidad de un medicamento es la combinación de
-- nombre + dosis + unidad_medida_id + marca_id + via_admin_id +
-- presentacion_id, permitiendo el mismo nombre con dosis o
-- catálogos distintos.
-- Uso: ejecutar UNA sola vez en MySQL; el unique resultante es
-- idéntico al declarado en medicamentoLogEntity (@Table uniqueConstraints).
-- ============================================================
ALTER TABLE medicamentos_log DROP INDEX uk_medicamento_log_nombre;
ALTER TABLE medicamentos_log
	ADD CONSTRAINT uk_medicamento_log_identidad
	UNIQUE (nombre, dosis, unidad_medida_id, marca_id, via_admin_id, presentacion_id);