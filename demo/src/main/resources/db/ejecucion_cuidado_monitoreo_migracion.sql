-- =====================================================================
-- Propósito: vincular la ejecución de una atención de control con el
--   registro de monitoreo que la ejecutó (signos vitales, glucosa o
--   control hídrico).
-- Uso: ejecutar UNA sola vez antes de arrancar la aplicación con
--   `spring.jpa.hibernate.ddl-auto=validate`.
-- Ejecución/precauciones:
--   - No es destructiva: agrega columnas nullable y sus llaves foráneas.
--   - Ejecutar sobre la base de datos real una única vez.
--   - En entornos locales con `ddl-auto=update` Hibernate puede crear
--     las columnas al arrancar; aun así el script deja las restricciones
--     explícitas para los entornos con `validate`.
-- =====================================================================

ALTER TABLE ejecuciones_cuidado
    ADD COLUMN signos_vitales_id BIGINT NULL,
    ADD COLUMN glucosa_id BIGINT NULL,
    ADD COLUMN control_hidrico_id BIGINT NULL;

ALTER TABLE ejecuciones_cuidado
    ADD CONSTRAINT fk_ejecucion_cuidado_signos
        FOREIGN KEY (signos_vitales_id) REFERENCES signos_vitales (id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_ejecucion_cuidado_glucosa
        FOREIGN KEY (glucosa_id) REFERENCES glucosa (id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_ejecucion_cuidado_hidrico
        FOREIGN KEY (control_hidrico_id) REFERENCES control_hidrico (id)
        ON DELETE SET NULL;
