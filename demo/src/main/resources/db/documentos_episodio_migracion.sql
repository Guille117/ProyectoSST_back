-- ============================================================
-- documentos_episodio_migracion.sql
-- Propósito: Crear la tabla de documentos por episodio y migrar
--            las rutas que antes se guardaban en
--            `referencias.url_documento`.
-- Uso:       Ejecutar UNA sola vez en MySQL (sst_db).
--            ddl-auto=update crea la tabla, pero no las FK ni migra datos.
-- ============================================================

CREATE TABLE IF NOT EXISTS documentos_episodio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    episodio_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    url_archivo VARCHAR(500) NOT NULL,
    fecha_subida DATETIME NOT NULL,
    usuario_id BIGINT NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    INDEX idx_documentos_episodio_episodio_tipo (episodio_id, tipo),
    CONSTRAINT fk_documentos_episodio_episodio FOREIGN KEY (episodio_id) REFERENCES episodios (id),
    CONSTRAINT fk_documentos_episodio_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- Migrar la ruta única de referencia a la nueva tabla.
INSERT INTO documentos_episodio (
    episodio_id, tipo, nombre_original, url_archivo, fecha_subida, usuario_id, estado
)
SELECT
    e.id,
    'REFERENCIA',
    SUBSTRING_INDEX(r.url_documento, '/', -1),
    r.url_documento,
    NOW(),
    e.medico_id,
    TRUE
FROM episodios e
INNER JOIN referencias r ON r.id = e.referencia_id
WHERE r.url_documento IS NOT NULL
  AND r.url_documento <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM documentos_episodio d
      WHERE d.episodio_id = e.id
        AND d.url_archivo = r.url_documento
  );

-- Eliminar la columna antigua: la nueva tabla es la única fuente de verdad.
ALTER TABLE referencias DROP COLUMN url_documento;

-- Verificación opcional:
-- SELECT * FROM documentos_episodio;
-- SHOW CREATE TABLE documentos_episodio;
