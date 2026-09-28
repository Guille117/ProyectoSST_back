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