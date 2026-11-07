-- =====================================================================
-- Propósito: agregar el estado lógico del comprobante de compras para
--            permitir su baja lógica sin eliminar la compra ni el archivo
--            físico. Si comprobante_estado = 0, el comprobante no se
--            muestra ni se sirve, pero la compra sigue activa y puede
--            recibir otro comprobante.
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación.
--       Con `ddl-auto=validate` Hibernate no modifica el esquema.
-- Ejecución/precauciones:
--   - No es destructiva: solo agrega una columna con valor por defecto.
--   - Las compras existentes quedan con comprobante_estado = 1 (visible).
-- =====================================================================

ALTER TABLE compras
    ADD COLUMN comprobante_estado TINYINT(1) NOT NULL DEFAULT 1 AFTER comprobante;
