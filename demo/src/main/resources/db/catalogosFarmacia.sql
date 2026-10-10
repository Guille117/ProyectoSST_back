-- ============================================================
-- Propósito: Stored procedure "catalogosFarmacia"
-- Cuenta los registros de las 4 tablas de catálogo de farmacia
-- (unidad_medidas, marcas, vias_admin, presentaciones) en una
-- sola consulta UNION ALL.
-- Uso: lo invoca el backend desde medicamentoLogService
-- (obtenerConteoCatalogosFarmacia) vía EntityManager.createStoredProcedureQuery.
-- Ejecutar una sola vez en MySQL para crear el procedimiento.
-- ============================================================
DROP PROCEDURE IF EXISTS catalogosFarmacia;

DELIMITER //
CREATE PROCEDURE catalogosFarmacia()
BEGIN
    SELECT 'unidad_medidas' AS nombreTabla, COUNT(*) AS registros FROM unidad_medidas
    UNION ALL
    SELECT 'marcas' AS nombreTabla, COUNT(*) AS registros FROM marcas
    UNION ALL
    SELECT 'vias_admin' AS nombreTabla, COUNT(*) AS registros FROM vias_admin
    UNION ALL
    SELECT 'presentaciones' AS nombreTabla, COUNT(*) AS registros FROM presentaciones;
END//
DELIMITER ;