-- ============================================================
-- lotes_migracion_fecha_vencimiento_opcional.sql
-- Propósito: Permitir NULL en `lotes.fecha_vencimiento` para que un
--            lote de INSUMO pueda registrarse sin fecha de vencimiento.
--            En lotes de MEDICAMENTO la fecha sigue siendo obligatoria
--            (validado en LoteService.crear/actualizar y en
--            CompraService.validarLotes).
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db) después de
--            desplegar el backend con LoteEntity sin `nullable = false`.
--            Es obligatorio porque ddl-auto=update NO relaja un NOT NULL
--            ya existente en una tabla creada con la versión anterior.
--            Lo consumen: POST/PUT /api/v1/lotes y el alta de lotes
--            dentro de POST /api/v1/compras (LoteService.crear).
-- ============================================================

ALTER TABLE lotes
    MODIFY COLUMN fecha_vencimiento DATE NULL;

-- Verificación opcional:
-- SHOW COLUMNS FROM lotes LIKE 'fecha_vencimiento';
-- La columna debe quedar con Null = YES.
