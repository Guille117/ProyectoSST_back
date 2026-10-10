-- ============================================================
-- compras_migracion.sql
-- Propósito: Agregar a la tabla `compras` la fecha automática
--            de creación y la relación obligatoria con `proveedores`.
--            Incluye la constraint FK que ddl-auto=update NO genera.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db) después de
--            desplegar el backend con los cambios de CompraEntity.
--            El backend (CompraService) inserta `fecha` y
--            `proveedor_id` en cada INSERT; este script solo
--            asegura columnas + FK en BDs existentes.
-- ============================================================

-- 1) Columnas (ddl-auto=update las crea, pero se listan por si se
--    ejecuta en una BD que aún no arrancó con el código nuevo).
ALTER TABLE compras
    ADD COLUMN fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN proveedor_id BIGINT NOT NULL;

-- 2) FK hacia proveedores: ddl-auto=update NO genera constraints,
--    por eso este paso es manual y obligatorio.
ALTER TABLE compras
    ADD CONSTRAINT fk_compras_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedores (id);

-- Nota: si la tabla `compras` ya tiene filas previas, la columna
-- fecha recibirá CURRENT_TIMESTAMP por defecto y proveedor_id
-- quedará en 0 (inválido) hasta asignarse manualmente o migrarse
-- con un UPDATE específico. En una BD nueva/vacía no hay impacto.
